package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.config.RabbitMQConfig;
import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.exception.KeycloakUserAlreadyExistsException;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.message.ProfesseurActivationMessage;
import com.App.lbs_backend.dto.request.ProfesseurRequest;
import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.mapper.ProfesseurMapper;
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
                entity.setKeycloakId(existingId);
                keycloakAdminService.assignRoleToUser(existingId, ROLE_PROFESSEUR);
            }
            return false;
        }
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

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String genererCode() {
        return "PROF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
