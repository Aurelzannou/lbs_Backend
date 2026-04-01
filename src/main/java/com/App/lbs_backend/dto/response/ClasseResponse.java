package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ClasseResponse(
        Long id,
        String uuid,
        Long profId,
        String code,
        String libelle,
        Long niveauId,
        Integer capaciteMax,
        Boolean actif,
        LocalDateTime modifierLe,
        String modifierPar,
        ProfesseurResponse professeur,
        NiveauResponse niveau
) {}