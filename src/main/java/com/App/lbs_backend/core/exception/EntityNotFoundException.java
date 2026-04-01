package com.App.lbs_backend.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String domain, String id) {
        super(String.format("L'entité %s avec l'identifiant %s est introuvable.", domain, id));
    }
}
