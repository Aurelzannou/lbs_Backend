package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.entity.Tuteur;
import org.springframework.stereotype.Component;

@Component
public class TuteurMapper implements Mapper<Tuteur, TuteurResponse> {

    @Override
    public TuteurResponse toResponse(Tuteur entity) {
        if (entity == null) return null;
        return new TuteurResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getTelephone1(),
                entity.getTelephone2(),
                entity.getEmail(),
                entity.getProfession(),
                entity.getAdresse(),
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}
