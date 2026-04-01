package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.NoteResponse;
import com.App.lbs_backend.entity.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    private final DossierEleveMapper dossierEleveMapper;
    private final MatiereMapper matiereMapper;
    private final PeriodeAcademiqueMapper periodeMapper;
    private final ProfesseurMapper professeurMapper;
    private final UtilisateurMapper utilisateurMapper;

    public NoteMapper(DossierEleveMapper dossierEleveMapper,
                      MatiereMapper matiereMapper,
                      PeriodeAcademiqueMapper periodeMapper,
                      ProfesseurMapper professeurMapper,
                      UtilisateurMapper utilisateurMapper) {
        this.dossierEleveMapper = dossierEleveMapper;
        this.matiereMapper = matiereMapper;
        this.periodeMapper = periodeMapper;
        this.professeurMapper = professeurMapper;
        this.utilisateurMapper = utilisateurMapper;
    }

    public NoteResponse toResponse(Note entity) {
        if (entity == null) return null;
        return new NoteResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getDossierEleveId(),
                entity.getMatiereId(),
                entity.getPeriodeId(),
                entity.getProfesseurId(),
                entity.getValeur(),
                entity.getBareme(),
                entity.getTypeEvaluation(),
                entity.getDateEvaluation(),
                entity.getCommentaire(),
                entity.getUtilisateurId(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                entity.getDossierEleve() != null ? dossierEleveMapper.toResponse(entity.getDossierEleve()) : null,
                matiereMapper.toResponse(entity.getMatiere()),
                periodeMapper.toResponse(entity.getPeriode()),
                professeurMapper.toResponse(entity.getProfesseur()),
                utilisateurMapper.toResponse(entity.getUtilisateur())
        );
    }
}
