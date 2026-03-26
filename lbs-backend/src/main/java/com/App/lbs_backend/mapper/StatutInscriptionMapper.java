package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.StatutInscriptionResponse;
import com.App.lbs_backend.entity.StatutInscription;
import org.springframework.stereotype.Component;

@Component
public class StatutInscriptionMapper implements Mapper<StatutInscription, StatutInscriptionResponse> {

    @Override
    public StatutInscriptionResponse toResponse(StatutInscription entity) {
        if (entity == null) return null;
        return new StatutInscriptionResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}
