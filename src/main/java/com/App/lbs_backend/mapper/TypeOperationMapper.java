package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.TypeOperationResponse;
import com.App.lbs_backend.entity.TypeOperation;
import org.springframework.stereotype.Component;

@Component
public class TypeOperationMapper implements Mapper<TypeOperation, TypeOperationResponse> {

    @Override
    public TypeOperationResponse toResponse(TypeOperation entity) {
        if (entity == null) return null;
        return new TypeOperationResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getDescription(),
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}