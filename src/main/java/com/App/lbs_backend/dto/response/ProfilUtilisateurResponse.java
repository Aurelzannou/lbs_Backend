package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ProfilUtilisateurResponse(
        Long id,
        String uuid,
        Long profilId,
        Long utilisateurId,
        String token,
        LocalDateTime modifierLe,
        String modifierPar,
        ProfilResponse profil,
        UtilisateurResponse utilisateur
) {}