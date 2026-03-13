package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ProfilUtilisateurResponse(
        Integer id,
        String uuid,
        Integer profilId,
        Integer utilisateurId,
        String token,
        LocalDateTime modifierLe,
        String modifierPar,
        ProfilResponse profil,
        UtilisateurResponse utilisateur
) {}