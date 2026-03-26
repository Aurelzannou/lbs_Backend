package com.App.lbs_backend.core.http.response;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        MetaResponse meta
) {
}
