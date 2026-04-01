package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ClasseResponse;
import com.App.lbs_backend.entity.Classe;
import org.springframework.stereotype.Component;

@Component
public class ClasseMapper implements Mapper<Classe, ClasseResponse> {

    private final ProfesseurMapper professeurMapper;
    private final NiveauMapper niveauMapper;

    public ClasseMapper(ProfesseurMapper professeurMapper, NiveauMapper niveauMapper) {
        this.professeurMapper = professeurMapper;
        this.niveauMapper = niveauMapper;
    }

    @Override
    public ClasseResponse toResponse(Classe entity) {
        if (entity == null) return null;
        return new ClasseResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getProfId(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getNiveauId(),
                entity.getCapaciteMax(),
                entity.getActif(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                professeurMapper.toResponse(entity.getProfesseur()),
                niveauMapper.toResponse(entity.getNiveau())
        );
    }
}