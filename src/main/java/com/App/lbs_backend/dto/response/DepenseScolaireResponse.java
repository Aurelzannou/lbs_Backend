package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DepenseScolaireResponse(
        Long id,
        String uuid,
        String code,
        String reference,
        Long caisseId,
        Long categorieDepenseId,
        Double montant,
        LocalDate dateDepense,
        String motif,
        Long utilisateurId,
        LocalDateTime modifierLe,
        CaisseResponse caisse,
        CategorieDepenseResponse categorieDepense,
        UtilisateurResponse utilisateur
) {}