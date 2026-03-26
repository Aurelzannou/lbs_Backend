package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.NiveauResponse;
import com.App.lbs_backend.entity.Niveau;
import org.springframework.stereotype.Component;

@Component
public class NiveauMapper implements Mapper<Niveau, NiveauResponse> {

    @Override
    public NiveauResponse toResponse(Niveau entity) {
        if (entity == null) return null;
        return new NiveauResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}
