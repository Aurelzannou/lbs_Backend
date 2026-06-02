package com.App.lbs_backend.dto.response;

import java.util.List;

public record UtilisateurResponse(
        Long id,
        String uuid,
        String nom,
        String prenom,
        String login,
        String email,
        String photo,
        String sexe,
        String keycloack,
        List<String> profils
) {}
