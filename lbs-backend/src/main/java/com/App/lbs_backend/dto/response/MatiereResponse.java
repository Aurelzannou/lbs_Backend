package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record MatiereResponse(
        Integer id,
        String uuid,
        String code,
        String libelle,
        Boolean actif,
        LocalDateTime modifierLe,
        String modifierPar
) {}
