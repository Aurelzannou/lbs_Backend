package com.App.lbs_backend.core.http.request;

import java.util.List;

public record UuidsRequest(List<String> ids) {
    public static UuidsRequest of(String... ids) {
        return new UuidsRequest(List.of(ids));
    }
}
