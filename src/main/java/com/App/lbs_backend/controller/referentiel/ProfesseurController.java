package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.config.RabbitMQConfig;
import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.exception.KeycloakUserAlreadyExistsException;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import org.springframework.dao.DataIntegrityViolationException;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.message.ProfesseurActivationMessage;
import com.App.lbs_backend.dto.request.ProfesseurRequest;
import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.mapper.ProfesseurMapper;
import com.App.lbs_backend.repository.ProfesseurRepository;
import com.App.lbs_backend.service.KeycloakAdminService;
import com.App.lbs_backend.service.referentiel.ProfesseurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/professeurs")
@RequiredArgsConstructor
@Slf4j
public class ProfesseurController extends MasterController<Professeur, ProfesseurResponse, ProfesseurRequest> {

    private static final String ROLE_PROFESSEUR = "PROFESSEUR";
    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private final SecureRandom random = new SecureRandom();

    private final ProfesseurService professeurService;
    private final ProfesseurRepository professeurRepository;
    private final ProfesseurMapper professeurMapper;
    private final KeycloakAdminService keycloakAdminService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Override
    protected AbstractBaseService<Professeur, ProfesseurResponse> service() {
        return professeurService;
    }

    @Override
    protected ProfesseurResponse doCreate(ProfesseurRequest form) {
        verifierEmailUnique(form.getEmail(), null);
        verifierCompteConnexionDisponible(form.getEmail(), null);

        Professeur entity = new Professeur();
        // Un professeur n'a pas de matricule saisi — code interne généré automatiquement.
        entity.setCode(isBlank(form.getCode()) ? genererCode() : form.getCode());
        entity.setNom(form.getNom());
        entity.setPrenom(form.getPrenom());
        entity.setEmail(form.getEmail());
        entity.setResidence(form.getResidence());
        entity.setNum(form.getNum());
        entity.setActif(form.getActif());
        entity.setMatiereIds(form.getMatiereIds());
        entity.setClasseIds(form.getClasseIds());

        Professeur saved = professeurService.create(entity);

        boolean compteProvisionne = false;
        if (!isBlank(saved.getEmail())) {
            compteProvisionne = provisionnerCompteKeycloak(saved);
            professeurService.update(saved);
        }

        return professeurMapper.toResponse(saved, compteProvisionne);
    }

    @Override
    protected ProfesseurResponse doUpdate(String uuid, ProfesseurRequest form) {
        Professeur entity = professeurService.findByUuid(uuid);
        verifierEmailUnique(form.getEmail(), entity.getId());
        verifierCompteConnexionDisponible(form.getEmail(), entity.getId());
        Boolean ancienActif = entity.getActif();

        // Le formulaire ne renvoie plus de code — on garde celui déjà en base tel quel.
        if (!isBlank(form.getCode())) entity.setCode(form.getCode());
        entity.setNom(form.getNom());
        entity.setPrenom(form.getPrenom());
        entity.setEmail(form.getEmail());
        entity.setResidence(form.getResidence());
        entity.setNum(form.getNum());
        entity.setActif(form.getActif());
        entity.setMatiereIds(form.getMatiereIds());
        entity.setClasseIds(form.getClasseIds());

        boolean compteProvisionne = false;
        if (!isBlank(entity.getEmail()) && isBlank(entity.getKeycloakId())) {
            compteProvisionne = provisionnerCompteKeycloak(entity);
        } else if (entity.getKeycloakId() != null
                && ancienActif != null && !ancienActif.equals(form.getActif())) {
            keycloakAdminService.setUserEnabled(entity.getKeycloakId(), Boolean.TRUE.equals(form.getActif()));
        }

        professeurService.update(entity);
        return professeurMapper.toResponse(entity, compteProvisionne);
    }

    /**
     * Suppression d'un professeur. On tente une suppression définitive ; si des données y sont
     * rattachées (classes, emploi du temps, notes, présences…), on bascule en désactivation
     * (actif = false). Dans les deux cas, le compte de connexion Keycloak est désactivé.
     */
    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        for (String uuid : uuids.ids()) {
            Professeur prof = professeurService.findByUuid(uuid);

            if (!isBlank(prof.getKeycloakId())) {
                keycloakAdminService.setUserEnabled(prof.getKeycloakId(), false);
            }

            try {
                professeurRepository.delete(prof);
                professeurRepository.flush();
                log.info("Professeur {} {} supprimé définitivement.", prof.getNom(), prof.getPrenom());
            } catch (DataIntegrityViolationException e) {
                log.warn("Professeur {} lié à d'autres données — désactivation au lieu de suppression : {}",
                        uuid, e.getMostSpecificCause().getMessage());
                prof.setActif(false);
                professeurRepository.saveAndFlush(prof);
            }
        }
        return true;
    }

    /** Génère un nouveau lien d'activation pour ce professeur — utile s'il a perdu le précédent
        ou si l'email d'origine n'a pas pu être envoyé (compte réutilisé). Le mot de passe n'est
        modifié qu'au moment où le professeur clique le lien et en choisit un lui-même. */
    @PutMapping("/{uuid}/reinitialiser-mot-de-passe")
    public ResponseEntity<?> reinitialiserMotDePasse(@PathVariable String uuid) {
        Professeur entity = professeurService.findByUuid(uuid);
        if (isBlank(entity.getKeycloakId())) {
            throw new IllegalArgumentException(
                    "Ce professeur n'a pas encore de compte de connexion — renseignez un email pour lui en créer un.");
        }
        genererEtEnvoyerLienActivation(entity);
        professeurService.update(entity);
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Nouveau lien d'activation envoyé par email à " + entity.getEmail(), null, request.getRequestURI()));
    }

    /** Crée le compte Keycloak du professeur (login = email), lui assigne le rôle PROFESSEUR et
        lui envoie un lien d'activation pour qu'il choisisse lui-même son mot de passe. Si un
        compte existe déjà avec cet email (ex: la même adresse sert déjà à un Tuteur), on relie ce
        compte existant sans toucher à son mot de passe ni lui envoyer de lien.
        @return true si un nouveau compte a été créé et le lien d'activation envoyé. */
    private boolean provisionnerCompteKeycloak(Professeur entity) {
        // Mot de passe interne jetable, jamais divulgué — écrasé dès que le professeur active
        // son compte via le lien reçu par email.
        String motDePasseJetable = genererMotDePasse();
        try {
            String keycloakId = keycloakAdminService.createUser(
                    entity.getEmail(), entity.getEmail(), entity.getNom(), entity.getPrenom(), motDePasseJetable);
            entity.setKeycloakId(keycloakId);
            keycloakAdminService.assignRoleToUser(keycloakId, ROLE_PROFESSEUR);
            genererEtEnvoyerLienActivation(entity);
            return true;
        } catch (KeycloakUserAlreadyExistsException e) {
            log.warn("Compte Keycloak déjà existant pour {} — réutilisation sans envoi de lien d'activation.", entity.getEmail());
            String existingId = keycloakAdminService.findUserIdByUsername(entity.getEmail());
            if (existingId != null) {
                // Garde-fou : ce compte de connexion est-il déjà relié à une autre fiche professeur ?
                // (colonne keycloak_id unique) — on refuse proprement plutôt que de laisser la
                // contrainte d'unicité échouer en base.
                professeurRepository.findByKeycloakId(existingId).ifPresent(autre -> {
                    if (!autre.getId().equals(entity.getId())) {
                        throw new IllegalArgumentException(compteDejaUtiliseMessage(entity.getEmail(), autre));
                    }
                });
                entity.setKeycloakId(existingId);
                keycloakAdminService.assignRoleToUser(existingId, ROLE_PROFESSEUR);
                // Le compte a pu être désactivé lors d'une suppression précédente de cette fiche —
                // on le réactive si le professeur réintégré est actif.
                if (Boolean.TRUE.equals(entity.getActif())) {
                    keycloakAdminService.setUserEnabled(existingId, true);
                }
            }
            return false;
        }
    }

    /** Vérifie qu'aucune autre fiche professeur n'est déjà rattachée au compte de connexion
        Keycloak correspondant à cet email. Appelé AVANT toute écriture en base pour éviter de
        créer une fiche orpheline, puis la contrainte d'unicité keycloak_id qui échoue en 500. */
    private void verifierCompteConnexionDisponible(String email, Long excludeId) {
        if (isBlank(email)) return;
        String existingId = keycloakAdminService.findUserIdByUsername(email);
        if (existingId == null) return;
        professeurRepository.findByKeycloakId(existingId).ifPresent(autre -> {
            if (!autre.getId().equals(excludeId)) {
                throw new IllegalArgumentException(compteDejaUtiliseMessage(email, autre));
            }
        });
    }

    private String compteDejaUtiliseMessage(String email, Professeur autre) {
        return "Un compte de connexion existe déjà pour « " + email + " » et il est rattaché au professeur "
                + autre.getPrenom() + " " + autre.getNom()
                + (isBlank(autre.getEmail()) ? "" : " (" + autre.getEmail() + ")")
                + ". Utilisez une autre adresse email, ou corrigez la fiche de ce professeur.";
    }

    private void genererEtEnvoyerLienActivation(Professeur entity) {
        String token = UUID.randomUUID().toString();
        entity.setActivationToken(token);
        entity.setActivationTokenExpiration(LocalDateTime.now().plusHours(48));

        String lien = frontendUrl + "/activer-compte?token=" + token;
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.PROFESSEUR_ACTIVATION_ROUTING_KEY,
                    new ProfesseurActivationMessage(entity.getEmail(), entity.getNom(), entity.getPrenom(), lien));
        } catch (Exception e) {
            log.error("Erreur publication lien d'activation professeur {} : {}", entity.getEmail(), e.getMessage());
        }
    }

    private String genererMotDePasse() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    /** L'email sert d'identifiant de connexion Keycloak et de clé de résolution du professeur
        connecté (ProfesseurRepository.findByEmail) — un doublon fait planter le portail
        professeur, donc on le refuse ici avec un message clair plutôt que de laisser la
        contrainte d'unicité en base échouer bruyamment. */
    private void verifierEmailUnique(String email, Long excludeId) {
        if (isBlank(email)) return;
        professeurRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(excludeId)) {
                throw new IllegalArgumentException("Un autre professeur utilise déjà cet email : " + email);
            }
        });
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String genererCode() {
        return "PROF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
