package com.App.lbs_backend.dto.response;

/**
 * Données renvoyées au portail parent pour ouvrir le widget FedaPay inline.
 * La clé publique est publique par nature : elle peut transiter vers le navigateur.
 */
public record PaiementInitResponse(
        long fedapayTransactionId,
        String publicKey,
        long montant,
        String devise,
        String description,
        /** URL de la page FedaPay hébergée — repli si le widget inline échoue. */
        String checkoutUrl
) {}
