package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.ProfesseurRequest;
import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.mapper.ProfesseurMapper;
import com.App.lbs_backend.service.KeycloakAdminService;
import com.App.lbs_backend.service.referentiel.ProfesseurService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/professeurs")
@RequiredArgsConstructor
public class ProfesseurController extends MasterController<Professeur, ProfesseurResponse, ProfesseurRequest> {

    private static final String ROLE_PROFESSEUR = "PROFESSEUR";
    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private final SecureRandom random = new SecureRandom();

    private final ProfesseurService professeurService;
    private final ProfesseurMapper professeurMapper;
    private final KeycloakAdminService keycloakAdminService;

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

        String motDePasseGenere = null;
        if (!isBlank(saved.getEmail())) {
            motDePasseGenere = provisionnerCompteKeycloak(saved);
            professeurService.update(saved);
        }

        return professeurMapper.toResponse(saved, motDePasseGenere);
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

        String motDePasseGenere = null;
        if (!isBlank(entity.getEmail()) && isBlank(entity.getKeycloakId())) {
            motDePasseGenere = provisionnerCompteKeycloak(entity);
        } else if (entity.getKeycloakId() != null
                && ancienActif != null && !ancienActif.equals(form.getActif())) {
            keycloakAdminService.setUserEnabled(entity.getKeycloakId(), Boolean.TRUE.equals(form.getActif()));
        }

        professeurService.update(entity);
        return professeurMapper.toResponse(entity, motDePasseGenere);
    }

    /** Crée le compte Keycloak du professeur (login = email) et lui assigne le rôle PROFESSEUR.
        Retourne le mot de passe généré en clair, à afficher une seule fois à l'admin. */
    private String provisionnerCompteKeycloak(Professeur entity) {
        String motDePasse = genererMotDePasse();
        String keycloakId = keycloakAdminService.createUser(
                entity.getEmail(), entity.getEmail(), entity.getNom(), entity.getPrenom(), motDePasse);
        entity.setKeycloakId(keycloakId);
        keycloakAdminService.assignRoleToUser(keycloakId, ROLE_PROFESSEUR);
        return motDePasse;
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
