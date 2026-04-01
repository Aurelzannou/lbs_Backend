package com.App.lbs_backend.core.specs;

public record FilterCriteria(
        String key,
        String operation, // e.g., ':', '<', '>', '~'
        Object value
) {
}
