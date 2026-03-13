package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.CategorieDepenseResponse;
import com.App.lbs_backend.entity.CategorieDepense;
import org.springframework.stereotype.Component;

@Component
public class CategorieDepenseMapper implements Mapper<CategorieDepense, CategorieDepenseResponse> {

    @Override
    public CategorieDepenseResponse toResponse(CategorieDepense entity) {
        if (entity == null) return null;
        return new CategorieDepenseResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getActif(),
                entity.getModifierLe()
        );
    }
}