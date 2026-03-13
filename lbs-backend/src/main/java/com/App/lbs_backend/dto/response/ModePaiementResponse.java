package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ModePaiementResponse(
        Integer id,
        String uuid,
        String code,
        String libelle,
        Boolean actif,
        LocalDateTime modifierLe
) {}