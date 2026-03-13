package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record FraisScolaireResponse(
        Integer id,
        String uuid,
        String code,
        Integer anneeScolaireId,
        Integer classeId,
        Integer typeFraisId,
        Double montant,
        Boolean actif,
        LocalDateTime modifierLe,
        String modifierPar,
        AnneeScolaireResponse anneeScolaire,
        ClasseResponse classe,
        TypeFraisResponse typeFrais
) {}