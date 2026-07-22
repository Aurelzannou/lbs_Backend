package com.App.lbs_backend.dto.message;

public record DossierNotificationMessage(
        String type, // "ACCEPTE" | "REFUSE"
        String toEmail,
        String tuteurNom,
        String tuteurPrenom,
        String eleveNom,
        String elevePrenom,
        String classe,
        String anneeScolaire,
        String numeroDossier,
        String motif // null si ACCEPTE
) {
}
