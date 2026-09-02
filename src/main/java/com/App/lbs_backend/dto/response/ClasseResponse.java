package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ClasseResponse(
        Long id,
        String uuid,
        Long profId,
        String code,
        String libelle,
        Long niveauId,
        Integer capaciteMax,
        Boolean actif,
        List<Long> matiereIds,
        /** Coefficient par matière (résolu depuis le référentiel Coefficient du niveau de la classe). */
        Map<Long, Double> coefficients,
        LocalDateTime modifierLe,
        String modifierPar,
        ProfesseurResponse professeur,
        NiveauResponse niveau
) {}