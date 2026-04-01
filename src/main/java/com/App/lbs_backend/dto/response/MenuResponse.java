package com.App.lbs_backend.dto.response;

import java.util.List;

public record MenuResponse(
        Long id,
        String uuid,
        String code,
        String description,
        String path,
        Integer ordre,
        String titre,
        Long menuEnfantId,
        List<MenuResponse> listeMenuEnfant
) {}