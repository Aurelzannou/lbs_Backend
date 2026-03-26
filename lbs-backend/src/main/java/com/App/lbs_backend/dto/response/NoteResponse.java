package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoteResponse(
        Integer id,
        String uuid,
        Integer inscriptionId,
        Integer matiereId,
        Integer periodeId,
        Integer professeurId,
        Double valeur,
        Double bareme,
        String typeEvaluation,
        LocalDate dateEvaluation,
        String commentaire,
        Integer utilisateurId,
        LocalDateTime modifierLe,
        String modifierPar,
        InscriptionResponse inscription,
        MatiereResponse matiere,
        PeriodeAcademiqueResponse periode,
        ProfesseurResponse professeur,
        UtilisateurResponse utilisateur
) {}
