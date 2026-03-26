package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.EleveTuteurResponse;
import com.App.lbs_backend.entity.EleveTuteur;
import org.springframework.stereotype.Component;

@Component
public class EleveTuteurMapper implements Mapper<EleveTuteur, EleveTuteurResponse> {

    private final EleveMapper eleveMapper;
    private final TuteurMapper tuteurMapper;

    public EleveTuteurMapper(EleveMapper eleveMapper, TuteurMapper tuteurMapper) {
        this.eleveMapper = eleveMapper;
        this.tuteurMapper = tuteurMapper;
    }

    @Override
    public EleveTuteurResponse toResponse(EleveTuteur entity) {
        if (entity == null) return null;
        return new EleveTuteurResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getEleveId(),
                entity.getTuteurId(),
                entity.getLienParente(),
                entity.getContactUrgence(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                eleveMapper.toResponse(entity.getEleve()),
                tuteurMapper.toResponse(entity.getTuteur())
        );
    }
}
