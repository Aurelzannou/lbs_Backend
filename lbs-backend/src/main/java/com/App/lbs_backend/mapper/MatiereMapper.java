package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.MatiereResponse;
import com.App.lbs_backend.entity.Matiere;
import org.springframework.stereotype.Component;

@Component
public class MatiereMapper implements Mapper<Matiere, MatiereResponse> {

    @Override
    public MatiereResponse toResponse(Matiere entity) {
        if (entity == null) return null;
        return new MatiereResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getActif(),
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}
