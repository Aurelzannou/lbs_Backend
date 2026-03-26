package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record CoefficientResponse(
        Integer id,
        String uuid,
        Integer matiereId,
        Integer niveauId,
        Double valeur,
        LocalDateTime modifierLe,
        String modifierPar,
        MatiereResponse matiere,
        NiveauResponse niveau
) {}
