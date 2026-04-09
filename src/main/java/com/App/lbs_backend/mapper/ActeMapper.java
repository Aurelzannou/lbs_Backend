package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ActeResponse;
import com.App.lbs_backend.entity.Acte;
import org.springframework.stereotype.Component;

@Component
public class ActeMapper implements Mapper<Acte, ActeResponse> {

    private final TypeActeMapper typeActeMapper;
    
    public ActeMapper(TypeActeMapper typeActeMapper) {
        this.typeActeMapper = typeActeMapper;
    }

    @Override
    public ActeResponse toResponse(Acte entity) {
        if (entity == null) return null;
        return new ActeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getReference(),
                entity.getTypeActeId(),
                entity.getCheminFichier(),
                entity.getNomFichier(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                typeActeMapper.toResponse(entity.getTypeActe())
        );
    }
}