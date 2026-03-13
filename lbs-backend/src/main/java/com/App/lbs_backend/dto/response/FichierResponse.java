package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record FichierResponse(
        Integer id,
        String uuid,
        String repertoir,
        String nom,
        Long taille,
        LocalDateTime modifierLe,
        String modifierPar
) {}