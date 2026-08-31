package com.App.lbs_backend.dto.response;

/**
 * Statut consolidé d'un paiement d'inscription.
 * statut         : INITIE | SUCCES | ECHEC   (côté LBS, colonne lbs_paie_statut_transaction)
 * fedapayStatut  : pending | approved | declined | canceled | ...  (côté FedaPay)
 */
public record PaiementStatutResponse(
        String statut,
        String fedapayStatut,
        boolean paye
) {}
