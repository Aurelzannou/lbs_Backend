package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import org.springframework.stereotype.Component;

@Component
public class ProfesseurMapper implements Mapper<Professeur, ProfesseurResponse> {

    @Override
    public ProfesseurResponse toResponse(Professeur entity) {
        if (entity == null) return null;
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
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}