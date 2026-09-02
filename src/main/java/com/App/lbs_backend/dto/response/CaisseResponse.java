package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record CaisseResponse(
        Long id,
        String uuid,
        String code,
        String libelle,
        Double solde,
        Boolean actif,
        Long utilisateurId,
        String utilisateurNomComplet,
        LocalDateTime modifierLe,
        String modifierPar
) {}