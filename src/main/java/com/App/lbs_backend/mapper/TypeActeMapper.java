package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.TypeActeResponse;
import com.App.lbs_backend.entity.TypeActe;
import org.springframework.stereotype.Component;

@Component
public class TypeActeMapper implements Mapper<TypeActe, TypeActeResponse> {

    @Override
    public TypeActeResponse toResponse(TypeActe entity) {
        if (entity == null) return null;
        return new TypeActeResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getModifierPar(),
                entity.getModifierLe()
        );
    }
}