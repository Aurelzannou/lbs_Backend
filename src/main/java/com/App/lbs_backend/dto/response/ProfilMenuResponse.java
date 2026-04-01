package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ProfilMenuResponse(
        Long id,
        String uuid,
        Long profilId,
        Long menuId,
        String token,
        LocalDateTime modifierLe,
        String modifierPar,
        ProfilResponse profil,
        MenuResponse menu
) {}