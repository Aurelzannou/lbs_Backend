package com.App.lbs_backend.dto.request;

import lombok.Data;

/** Corps de POST /api/inscription/{dossierId}/paiement/init */
@Data
public class PaiementInitRequest {
    /** Numéro Mobile Money saisi par le parent (facultatif : FedaPay le redemande sinon). */
    private String telephonePaiement;

    /** Montant que le parent choisit de payer (FCFA). Facultatif : par défaut il solde le
        restant dû du frais. Doit être compris entre 1 et le reste à payer. */
    private Long montant;
}
