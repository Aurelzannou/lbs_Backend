package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.CoefficientResponse;
import com.App.lbs_backend.entity.Coefficient;
import org.springframework.stereotype.Component;

@Component
public class CoefficientMapper implements Mapper<Coefficient, CoefficientResponse> {

    private final MatiereMapper matiereMapper;
    private final NiveauMapper niveauMapper;

    public CoefficientMapper(MatiereMapper matiereMapper, NiveauMapper niveauMapper) {
        this.matiereMapper = matiereMapper;
        this.niveauMapper = niveauMapper;
    }

    @Override
    public CoefficientResponse toResponse(Coefficient entity) {
        if (entity == null) return null;
        return new CoefficientResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getMatiereId(),
                entity.getNiveauId(),
                entity.getValeur(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                matiereMapper.toResponse(entity.getMatiere()),
                niveauMapper.toResponse(entity.getNiveau())
        );
    }
}
