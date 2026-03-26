package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EleveResponse(
        Integer id,
        String uuid,
        String code,
        String nom,
        String prenom,
        String sexe,
        LocalDate dateNaissance,
        String matricule,
        Boolean actif,
        String souffrant,
        String provenance,
        String photo,
        Integer utilisateurId,
        LocalDateTime modifierLe,
        String modifierPar,
        UtilisateurResponse utilisateur
) {}
