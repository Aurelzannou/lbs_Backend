package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record FraisScolaireResponse(
        Long id,
        String uuid,
        String code,
        Long anneeScolaireId,
        Long classeId,
        Long typeFraisId,
        Double montant,
        Boolean actif,
        LocalDateTime modifierLe,
        String modifierPar,
        AnneeScolaireResponse anneeScolaire,
        ClasseResponse classe,
        TypeFraisResponse typeFrais
) {}