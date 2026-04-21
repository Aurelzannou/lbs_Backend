package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;

import java.time.LocalDate;

public record EleveRequest(
        String code,
        String nom,
        String prenom,
        String sexe,
        LocalDate dateNaissance,
        Boolean actif,
        Boolean souffrant,
        String provenance,
        String photo,
        Long utilisateurId,
        Long classeId
) implements FormRequest {}
