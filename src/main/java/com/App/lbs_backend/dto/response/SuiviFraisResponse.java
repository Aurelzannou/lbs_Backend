package com.App.lbs_backend.dto.response;

import java.util.List;

public record SuiviFraisResponse(
        Long fraisScolaireId,
        String typeFraisLibelle,
        Double montantDu,
        Double montantPaye,
        Double reste,
        List<SuiviTrancheResponse> tranches
) {}
