package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ProfilResponse;
import com.App.lbs_backend.entity.Profil;
import org.springframework.stereotype.Component;

@Component
public class ProfilMapper implements Mapper<Profil, ProfilResponse> {

    @Override
    public ProfilResponse toResponse(Profil entity) {
        if (entity == null) return null;
        return new ProfilResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle()
        );
    }
}