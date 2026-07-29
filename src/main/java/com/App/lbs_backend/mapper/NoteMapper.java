package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.NoteResponse;
import com.App.lbs_backend.entity.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    private final EleveMapper eleveMapper;
    private final MatiereMapper matiereMapper;
    private final PeriodeAcademiqueMapper periodeMapper;
    private final ProfesseurMapper professeurMapper;

    public NoteMapper(EleveMapper eleveMapper,
                      MatiereMapper matiereMapper,
                      PeriodeAcademiqueMapper periodeMapper,
                      ProfesseurMapper professeurMapper) {
        this.eleveMapper = eleveMapper;
        this.matiereMapper = matiereMapper;
        this.periodeMapper = periodeMapper;
        this.professeurMapper = professeurMapper;
    }

    public NoteResponse toResponse(Note entity) {
        if (entity == null) return null;
        return new NoteResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getEleveId(),
                entity.getMatiereId(),
                entity.getPeriodeId(),
                entity.getProfesseurId(),
                entity.getValeur(),
                entity.getBareme(),
                entity.getTypeEvaluation(),
                entity.getNumeroDevoir(),
                entity.getDateEvaluation(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                entity.getEleve() != null ? eleveMapper.toResponse(entity.getEleve()) : null,
                entity.getMatiere() != null ? matiereMapper.toResponse(entity.getMatiere()) : null,
                entity.getPeriode() != null ? periodeMapper.toResponse(entity.getPeriode()) : null,
                entity.getProfesseur() != null ? professeurMapper.toResponse(entity.getProfesseur()) : null
        );
    }
}
