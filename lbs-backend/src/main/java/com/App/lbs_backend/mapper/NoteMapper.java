package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.NoteResponse;
import com.App.lbs_backend.entity.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper implements Mapper<Note, NoteResponse> {

    private final InscriptionMapper inscriptionMapper;
    private final MatiereMapper matiereMapper;
    private final PeriodeAcademiqueMapper periodeMapper;
    private final ProfesseurMapper professeurMapper;
    private final UtilisateurMapper utilisateurMapper;

    public NoteMapper(InscriptionMapper inscriptionMapper,
                      MatiereMapper matiereMapper,
                      PeriodeAcademiqueMapper periodeMapper,
                      ProfesseurMapper professeurMapper,
                      UtilisateurMapper utilisateurMapper) {
        this.inscriptionMapper = inscriptionMapper;
        this.matiereMapper = matiereMapper;
        this.periodeMapper = periodeMapper;
        this.professeurMapper = professeurMapper;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    public NoteResponse toResponse(Note entity) {
        if (entity == null) return null;
        return new NoteResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getInscriptionId(),
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
                // Les relations complexes peuvent être nullables pour éviter les boucles infinies ou ralentissements
                // selon le besoin. Ici on les renseigne si les entités sont chargées (EAGER/LAZY initialisé)
                entity.getInscription() != null ? inscriptionMapper.toResponse(entity.getInscription()) : null,
                matiereMapper.toResponse(entity.getMatiere()),
                periodeMapper.toResponse(entity.getPeriode()),
                professeurMapper.toResponse(entity.getProfesseur()),
                utilisateurMapper.toResponse(entity.getUtilisateur())
        );
    }
}
