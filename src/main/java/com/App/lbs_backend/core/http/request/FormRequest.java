package com.App.lbs_backend.core.http.request;

public interface FormRequest {
    default String getCode() {
        return null;
    }
}
