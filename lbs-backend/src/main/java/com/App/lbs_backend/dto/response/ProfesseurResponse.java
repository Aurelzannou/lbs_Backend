package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ProfesseurResponse(
        Integer id,
        String uuid,
        String code,
        String nom,
        String prenom,
        String email,
        String residence,
        String num,
        Boolean actif,
        LocalDateTime modifierLe,
        String modifierPar
) {}