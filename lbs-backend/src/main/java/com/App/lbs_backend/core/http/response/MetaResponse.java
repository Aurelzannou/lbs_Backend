package com.App.lbs_backend.core.http.response;

import org.springframework.data.domain.Page;

public record MetaResponse(
        long totalElements,
        int totalPages,
        int size,
        int page
) {
    public static MetaResponse of() {
        return new MetaResponse(0, 0, 0, 0);
    }

    public static MetaResponse ofPage(Page<?> pageData) {
        return new MetaResponse(
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.getSize(),
                pageData.getNumber() + 1 // Spring pages are 0-indexed, we expose 1-indexed to frontend
        );
    }
}
