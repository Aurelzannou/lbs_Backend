package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.MatiereRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ProfesseurMapper implements Mapper<Professeur, ProfesseurResponse> {

    private final MatiereRepository matiereRepository;
    private final ClasseRepository classeRepository;

    public ProfesseurMapper(MatiereRepository matiereRepository, ClasseRepository classeRepository) {
        this.matiereRepository = matiereRepository;
        this.classeRepository = classeRepository;
    }

    @Override
    public ProfesseurResponse toResponse(Professeur entity) {
        return toResponse(entity, false);
    }

    /** Utilisé juste après une création/mise à jour ayant provisionné un nouveau compte de
        connexion (identifiants envoyés par email) — signale au frontend qu'un email vient de
        partir, sans jamais faire transiter le mot de passe par l'API. */
    public ProfesseurResponse toResponse(Professeur entity, boolean compteProvisionneMaintenant) {
        if (entity == null) return null;

        List<Long> matiereIds = entity.getMatiereIds() != null ? entity.getMatiereIds() : Collections.emptyList();
        List<String> matiereLibelles = matiereIds.stream()
                .map(id -> matiereRepository.findById(id).map(com.App.lbs_backend.entity.Matiere::getLibelle).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();

        List<Long> classeIds = entity.getClasseIds() != null ? entity.getClasseIds() : Collections.emptyList();
        List<String> classeLibelles = classeIds.stream()
                .map(id -> classeRepository.findById(id).map(com.App.lbs_backend.entity.Classe::getLibelle).orElse(null))
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
                classeIds,
                classeLibelles,
                compteProvisionneMaintenant
        );
    }
}