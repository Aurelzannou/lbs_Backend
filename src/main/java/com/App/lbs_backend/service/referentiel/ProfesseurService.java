package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.mapper.ProfesseurMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ProfesseurRepository;
import com.App.lbs_backend.service.KeycloakAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProfesseurService extends AbstractBaseService<Professeur, ProfesseurResponse> {

    private final ProfesseurRepository professeurRepository;
    private final ProfesseurMapper professeurMapper;
    private final KeycloakAdminService keycloakAdminService;

    public ProfesseurService(ProfesseurRepository professeurRepository, ProfesseurMapper professeurMapper,
                              KeycloakAdminService keycloakAdminService) {
        super(Professeur.class);
        this.professeurRepository = professeurRepository;
        this.professeurMapper = professeurMapper;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    public BaseRepository<Professeur> repository() {
        return professeurRepository;
    }

    @Override
    public Mapper<Professeur, ProfesseurResponse> mapper() {
        return professeurMapper;
    }

    /** Consomme un jeton d'activation à usage unique : vérifie sa validité, applique le mot de
        passe choisi par le professeur dans Keycloak, puis invalide le jeton. */
    @Transactional
    public void activerCompte(String token, String motDePasse) {
        Professeur professeur = professeurRepository.findByActivationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Ce lien d'activation est invalide ou a déjà été utilisé."));

        if (professeur.getActivationTokenExpiration() == null
                || professeur.getActivationTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ce lien d'activation a expiré. Demandez-en un nouveau à l'administration.");
        }

        if (professeur.getKeycloakId() == null) {
            throw new IllegalArgumentException("Aucun compte de connexion associé à ce professeur.");
        }

        keycloakAdminService.resetPassword(professeur.getKeycloakId(), motDePasse);

        professeur.setActivationToken(null);
        professeur.setActivationTokenExpiration(null);
        update(professeur);
    }
}
