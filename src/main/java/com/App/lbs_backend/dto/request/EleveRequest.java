package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;

import java.time.LocalDate;

public record EleveRequest(
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
        Long utilisateurId
) implements FormRequest {}
