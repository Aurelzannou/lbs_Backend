package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DepenseScolaireResponse(
        Integer id,
        String uuid,
        String code,
        String reference,
        Integer caisseId,
        Integer categorieDepenseId,
        Double montant,
        LocalDate dateDepense,
        String motif,
        Integer utilisateurId,
        LocalDateTime modifierLe,
        CaisseResponse caisse,
        CategorieDepenseResponse categorieDepense,
        UtilisateurResponse utilisateur
) {}