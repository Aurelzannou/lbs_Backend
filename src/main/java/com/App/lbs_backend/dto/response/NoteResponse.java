package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoteResponse(
        Long id,
        String uuid,
        Long dossierEleveId,
        Long matiereId,
        Long periodeId,
        Long professeurId,
        Double valeur,
        Double bareme,
        String typeEvaluation,
        LocalDate dateEvaluation,
        String commentaire,
        Long utilisateurId,
        LocalDateTime modifierLe,
        String modifierPar,
        DossierEleveResponse dossierEleve,
        MatiereResponse matiere,
        PeriodeAcademiqueResponse periode,
        ProfesseurResponse professeur,
        UtilisateurResponse utilisateur
) {}
