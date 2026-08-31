package com.App.lbs_backend.dto.response;

import java.util.List;

public record SuiviPaiementResponse(
        Long dossierEleveId,
        Double totalDu,
        Double totalPaye,
        Double totalReste,
        List<SuiviFraisResponse> frais
) {}
