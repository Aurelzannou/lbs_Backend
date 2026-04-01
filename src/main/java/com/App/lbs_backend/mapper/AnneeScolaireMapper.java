package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.AnneeScolaireResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import org.springframework.stereotype.Component;

@Component
public class AnneeScolaireMapper implements Mapper<AnneeScolaire, AnneeScolaireResponse> {

    @Override
    public AnneeScolaireResponse toResponse(AnneeScolaire entity) {
        if (entity == null) return null;
        return new AnneeScolaireResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getDateDebut(),
                entity.getDateFin(),
                entity.getActif(),
                entity.getModifierLe()
        );
    }
}