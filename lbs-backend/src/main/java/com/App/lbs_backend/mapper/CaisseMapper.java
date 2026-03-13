package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.CaisseResponse;
import com.App.lbs_backend.entity.Caisse;
import org.springframework.stereotype.Component;

@Component
public class CaisseMapper implements Mapper<Caisse, CaisseResponse> {

    @Override
    public CaisseResponse toResponse(Caisse entity) {
        if (entity == null) return null;
        return new CaisseResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getSolde(),
                entity.getActif(),
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}