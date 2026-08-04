package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoteResponse(
        Long id,
        String uuid,
        Long eleveId,
        Long matiereId,
        Long periodeId,
        Long professeurId,
        Double valeur,
        Double bareme,
        String typeEvaluation,
        Integer numero,
        LocalDate dateEvaluation,
        LocalDateTime modifierLe,
        String modifierPar,
        EleveResponse eleve,
        MatiereResponse matiere,
        PeriodeAcademiqueResponse periode,
        ProfesseurResponse professeur
) {}
