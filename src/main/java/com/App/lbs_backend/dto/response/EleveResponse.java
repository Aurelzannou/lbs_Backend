package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EleveResponse(
        Long id,
        String uuid,
        String code,
        String nom,
        String prenom,
        String sexe,
        LocalDate dateNaissance,
        Integer age,
        Boolean actif,
        Boolean souffrant,
        String provenance,
        String photo,
        Long utilisateurId,
        Long classeId,
        LocalDateTime modifierLe,
        String modifierPar,
        UtilisateurResponse utilisateur,
        ClasseResponse classe
) {}
