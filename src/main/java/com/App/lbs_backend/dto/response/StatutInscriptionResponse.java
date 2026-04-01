package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record StatutInscriptionResponse(
        Long id,
        String uuid,
        String code,
        String libelle,
        LocalDateTime modifierLe,
        String modifierPar
) {}
