package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record TypeFraisResponse(
        Integer id,
        String uuid,
        String code,
        String libelle,
        Boolean obligatoire,
        Boolean actif,
        LocalDateTime modifierLe
) {}