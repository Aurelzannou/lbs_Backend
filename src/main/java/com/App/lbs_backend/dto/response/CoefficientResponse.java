package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record CoefficientResponse(
        Long id,
        String uuid,
        Long matiereId,
        Long niveauId,
        Double valeur,
        LocalDateTime modifierLe,
        String modifierPar,
        MatiereResponse matiere,
        NiveauResponse niveau
) {}
