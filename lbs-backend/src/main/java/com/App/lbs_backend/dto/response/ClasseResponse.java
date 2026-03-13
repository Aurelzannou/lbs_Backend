package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ClasseResponse(
        Integer id,
        String uuid,
        Integer profId,
        String code,
        String libelle,
        Boolean actif,
        LocalDateTime modifierLe,
        String modifierPar,
        ProfesseurResponse professeur
) {}