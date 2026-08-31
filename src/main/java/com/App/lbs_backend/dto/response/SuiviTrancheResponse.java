package com.App.lbs_backend.dto.response;

import java.time.LocalDate;

public record SuiviTrancheResponse(
        Long echeancierId,
        Integer numero,
        String libelle,
        LocalDate dateEcheance,
        Double montant,
        Double montantAlloue,
        String statut
) {}
