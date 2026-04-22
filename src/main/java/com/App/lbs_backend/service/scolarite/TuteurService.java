package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.request.TuteurRequest;
import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.TuteurMapper;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.service.KeycloakAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TuteurService extends AbstractBaseService<Tuteur, TuteurResponse> {

    private final TuteurRepository tuteurRepository;
    private final TuteurMapper tuteurMapper;
    private final KeycloakAdminService keycloakAdminService;

    public TuteurService(TuteurRepository tuteurRepository, TuteurMapper tuteurMapper, 
                         KeycloakAdminService keycloakAdminService) {
        super(Tuteur.class);
        this.tuteurRepository = tuteurRepository;
        this.tuteurMapper = tuteurMapper;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    public BaseRepository<Tuteur> repository() {
        return tuteurRepository;
    }

    @Override
    public Mapper<Tuteur, TuteurResponse> mapper() {
        return tuteurMapper;
    }

    @Transactional
    public TuteurResponse register(TuteurRequest request) {
        log.info("Inscription d'un nouveau tuteur : {}", request.getEmail());
        
        if (tuteurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Un tuteur avec cet email existe déjà");
        }

        // 1. Création du compte dans Keycloak
        String keycloakId = keycloakAdminService.createUser(
                request.getEmail(), // On utilise l'email comme username
                request.getEmail(),
                request.getPrenom(),
                request.getNom(),
                request.getMotDePasse()
        );

        // 2. Assignation du rôle TUTEUR dans Keycloak
        try {
            keycloakAdminService.assignRoleToUser(keycloakId, "TUTEUR");
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation du rôle TUTEUR dans Keycloak pour {}", request.getEmail(), e);
        }

        // 3. Création locale du Tuteur
        Tuteur tuteur = new Tuteur();
        tuteur.setNom(request.getNom());
        tuteur.setPrenom(request.getPrenom());
        tuteur.setEmail(request.getEmail());
        tuteur.setTelephone1(request.getTelephone1());
        tuteur.setTelephone2(request.getTelephone2());
        tuteur.setProfession(request.getProfession());
        tuteur.setAdresse(request.getAdresse());
        tuteur.setCode(request.getCode());
        tuteur.setKeycloakId(keycloakId);
        tuteur.setActif(true);

        return mapper().toResponse(tuteurRepository.save(tuteur));
    }
}
