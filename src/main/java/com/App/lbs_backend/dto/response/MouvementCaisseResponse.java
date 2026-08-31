package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record MouvementCaisseResponse(
        Long id,
        String uuid,
        String code,
        Long caisseId,
        String caisseLibelle,
        String typeMouvement,
        Double montant,
        LocalDateTime dateMouvement,
        String source,
        Long sourceId,
        Double soldeApres,
        String description
) {}
