package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record TuteurResponse(
        Long id,
        String uuid,
        String code,
        String nom,
        String prenom,
        String telephone1,
        String telephone2,
        String email,
        String profession,
        String adresse,
        LocalDateTime modifierLe,
        String modifierPar
) {}
