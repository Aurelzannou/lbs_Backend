package com.App.lbs_backend.dto.response;

import java.util.List;

public record MenuResponse(
        Integer id,
        String uuid,
        String code,
        String description,
        String path,
        Integer ordre,
        String titre,
        Integer menuEnfantId,
        List<MenuResponse> listeMenuEnfant
) {}