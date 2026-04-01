package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record EleveTuteurResponse(
        Long id,
        String uuid,
        Long eleveId,
        Long tuteurId,
        String lienParente,
        Boolean contactUrgence,
        LocalDateTime modifierLe,
        String modifierPar,
        EleveResponse eleve,
        TuteurResponse tuteur
) {}
