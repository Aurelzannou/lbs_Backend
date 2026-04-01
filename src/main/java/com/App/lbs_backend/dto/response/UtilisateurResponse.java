package com.App.lbs_backend.dto.response;

public record UtilisateurResponse(
        Long id,
        String uuid,
        String nom,
        String prenom,
        String login,
        String photo,
        String sexe,
        String keycloack
) {}