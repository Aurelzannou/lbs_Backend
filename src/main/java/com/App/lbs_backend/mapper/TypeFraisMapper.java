package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.TypeFraisResponse;
import com.App.lbs_backend.entity.TypeFrais;
import org.springframework.stereotype.Component;

@Component
public class TypeFraisMapper implements Mapper<TypeFrais, TypeFraisResponse> {

    @Override
    public TypeFraisResponse toResponse(TypeFrais entity) {
        if (entity == null) return null;
        return new TypeFraisResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getObligatoire(),
                entity.getActif(),
                entity.getModifierLe()
        );
    }
}