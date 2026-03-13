package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record CaisseResponse(
        Integer id,
        String uuid,
        String code,
        String libelle,
        Double solde,
        Boolean actif,
        LocalDateTime modifierLe,
        String modifierPar
) {}