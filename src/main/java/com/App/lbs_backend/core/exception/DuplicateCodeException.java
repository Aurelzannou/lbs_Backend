package com.App.lbs_backend.core.exception;

import lombok.Getter;

@Getter
public class DuplicateCodeException extends RuntimeException {
    private final String domain;
    private final String code;

    public DuplicateCodeException(String domain, String code) {
        super(String.format("%s with code '%s' already exists.", domain, code));
        this.domain = domain;
        this.code = code;
    }
}
