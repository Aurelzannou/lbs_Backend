package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EcheancierResponse(
        Long id,
        String uuid,
        String code,
        Long fraisScolaireId,
        Integer numero,
        String libelle,
        LocalDate dateEcheance,
        Double montant,
        LocalDateTime modifierLe,
        String modifierPar,
        FraisScolaireResponse fraisScolaire
) {}