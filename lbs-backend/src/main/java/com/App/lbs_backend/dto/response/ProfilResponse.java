package com.App.lbs_backend.dto.response;

public record ProfilResponse(
        Integer id,
        String uuid,
        String code,
        String libelle
) {}