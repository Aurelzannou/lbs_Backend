package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime modifierLe,
        String modifierPar,
        ProfesseurResponse professeur,
        NiveauResponse niveau
) {}