package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaiementResponse(
        Integer id,
        String uuid,
        String code,
        String reference,
        Integer dossierId,
        Integer fraisScolaireId,
        LocalDate datePaiement,
        Double montant,
        Integer modePaiementId,
        Integer caisseId,
        Integer utilisateurId,
        String observation,
        LocalDateTime modifierLe,
        String modifierPar,
        DossierEleveResponse dossier,
        FraisScolaireResponse fraisScolaire,
        ModePaiementResponse modePaiement,
        CaisseResponse caisse,
        UtilisateurResponse utilisateur
) {}