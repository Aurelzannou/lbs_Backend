package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record HistoriqueDossierResponse(
    Long id,
    String action,
    String effectuePar,
    LocalDateTime effectueLe,
    String commentaire
) {}
