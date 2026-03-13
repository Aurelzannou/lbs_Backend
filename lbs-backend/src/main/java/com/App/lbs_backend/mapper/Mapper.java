package com.App.lbs_backend.mapper;

import org.springframework.stereotype.Component;

@Component
public interface Mapper<E, R> {

    R toResponse(E entity);

    default R toForm(E entity) {
        return null;
    }
}
