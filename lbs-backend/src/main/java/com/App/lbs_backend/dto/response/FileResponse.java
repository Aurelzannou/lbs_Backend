package com.App.lbs_backend.dto.response;

import java.io.InputStream;

public record FileResponse(
        String fileName,
        long length,
        String type,
        InputStream stream
) {
}
