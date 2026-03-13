package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record TypeOperationResponse(
        Integer id,
        String uuid,
        String code,
        String libelle,
        String description,
        LocalDateTime modifierLe,
        String modifierPar
) {}