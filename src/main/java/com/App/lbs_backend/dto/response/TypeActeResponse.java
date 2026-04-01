package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record TypeActeResponse(
        Long id,
        String uuid,
        String code,
        String libelle,
        String modifierPar,
        LocalDateTime modifierLe
) {}