package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ModePaiementResponse;
import com.App.lbs_backend.entity.ModePaiement;
import org.springframework.stereotype.Component;

@Component
public class ModePaiementMapper implements Mapper<ModePaiement, ModePaiementResponse> {

    @Override
    public ModePaiementResponse toResponse(ModePaiement entity) {
        if (entity == null) return null;
        return new ModePaiementResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getActif(),
                entity.getModifierLe()
        );
    }
}