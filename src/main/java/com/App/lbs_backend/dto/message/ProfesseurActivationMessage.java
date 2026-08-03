package com.App.lbs_backend.dto.message;

public record ProfesseurActivationMessage(
        String toEmail,
        String nom,
        String prenom,
        String lienActivation
) {
}
