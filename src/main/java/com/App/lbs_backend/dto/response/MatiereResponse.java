package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record MatiereResponse(
        Long id,
        String uuid,
        String code,
        String libelle,
        Boolean actif,
        Boolean estConduite,
        LocalDateTime modifierLe,
        String modifierPar
) {}
