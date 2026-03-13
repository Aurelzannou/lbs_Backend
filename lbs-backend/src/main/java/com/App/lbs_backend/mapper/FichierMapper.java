package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.FichierResponse;
import com.App.lbs_backend.entity.Fichier;
import org.springframework.stereotype.Component;

@Component
public class FichierMapper implements Mapper<Fichier, FichierResponse> {

    @Override
    public FichierResponse toResponse(Fichier entity) {
        if (entity == null) return null;
        return new FichierResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getRepertoir(),
                entity.getNom(),
                entity.getTaille(),
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}