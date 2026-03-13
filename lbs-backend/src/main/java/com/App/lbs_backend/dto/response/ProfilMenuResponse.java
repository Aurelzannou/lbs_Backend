package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ProfilMenuResponse(
        Integer id,
        String uuid,
        Integer profilId,
        Integer menuId,
        String token,
        LocalDateTime modifierLe,
        String modifierPar,
        ProfilResponse profil,
        MenuResponse menu
) {}