package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.repository.MatiereRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProfesseurMapper implements Mapper<Professeur, ProfesseurResponse> {

    private final MatiereRepository matiereRepository;

    public ProfesseurMapper(MatiereRepository matiereRepository) {
        this.matiereRepository = matiereRepository;
    }

    @Override
    public ProfesseurResponse toResponse(Professeur entity) {
        return toResponse(entity, null);
    }

    /** Utilisé juste après une création/mise à jour ayant généré un mot de passe de connexion,
        pour le renvoyer une seule fois dans la réponse HTTP sans jamais le persister. */
    public ProfesseurResponse toResponse(Professeur entity, String motDePasseGenere) {
        if (entity == null) return null;

        List<Long> matiereIds = entity.getMatiereIds() != null ? entity.getMatiereIds() : Collections.emptyList();
        List<String> matiereLibelles = matiereIds.stream()
                .map(id -> matiereRepository.findById(id).map(com.App.lbs_backend.entity.Matiere::getLibelle).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();

        return new ProfesseurResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getEmail(),
                entity.getResidence(),
                entity.getNum(),
                entity.getActif(),
                entity.getKeycloakId(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                matiereIds,
                matiereLibelles,
                motDePasseGenere
        );
    }
}