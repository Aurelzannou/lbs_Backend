package com.App.lbs_backend.mapper;

public interface Mapper<E, R> {

    R toResponse(E entity);

    default R toForm(E entity) {
        return null;
    }
}
