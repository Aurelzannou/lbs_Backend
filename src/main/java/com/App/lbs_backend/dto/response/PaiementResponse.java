package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaiementResponse(
        Long id,
        String uuid,
        String code,
        String reference,
        Long dossierEleveId,
        Long fraisScolaireId,
        LocalDate datePaiement,
        Double montant,
        Long modePaiementId,
        Long caisseId,
        Long utilisateurId,
        String observation,
        LocalDateTime modifierLe,
        String modifierPar,
        DossierEleveResponse dossierEleve,
        FraisScolaireResponse fraisScolaire,
        ModePaiementResponse modePaiement,
        CaisseResponse caisse,
        UtilisateurResponse utilisateur
) {}