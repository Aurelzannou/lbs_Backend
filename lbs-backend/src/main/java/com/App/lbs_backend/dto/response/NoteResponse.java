package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoteResponse(
        Integer id,
        String uuid,
        Integer dossierEleveId,
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
        DossierEleveResponse dossierEleve,
        MatiereResponse matiere,
        PeriodeAcademiqueResponse periode,
        ProfesseurResponse professeur,
        UtilisateurResponse utilisateur
) {}
