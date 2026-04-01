package com.App.lbs_backend.dto.response;

public record ProfilResponse(
        Long id,
        String uuid,
        String code,
        String libelle
) {}