package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.request.EtapeRequest;
import com.App.lbs_backend.dto.response.EtapeResponse;
import com.App.lbs_backend.entity.Etape;
import org.springframework.stereotype.Component;

@Component
public class EtapeMapper {

    public Etape toEntity(EtapeRequest request) {
        if (request == null) return null;
        Etape entity = new Etape();
        entity.setCode(request.getCode());
        entity.setLibelle(request.getLibelle());
        entity.setOrdre(request.getOrdre());
        entity.setActif(request.getActif() != null ? request.getActif() : true);
        return entity;
    }

    public void updateEntityFromRequest(EtapeRequest request, Etape entity) {
        if (request == null || entity == null) return;
        if (request.getCode() != null) entity.setCode(request.getCode());
        if (request.getLibelle() != null) entity.setLibelle(request.getLibelle());
        if (request.getOrdre() != null) entity.setOrdre(request.getOrdre());
        if (request.getActif() != null) entity.setActif(request.getActif());
    }

    public EtapeResponse toResponse(Etape entity) {
        if (entity == null) return null;
        EtapeResponse response = new EtapeResponse();
        response.setId(entity.getId());
        response.setUuid(entity.getUuid());
        response.setCode(entity.getCode());
        response.setLibelle(entity.getLibelle());
        response.setOrdre(entity.getOrdre());
        response.setActif(entity.getActif());
        response.setModifierLe(entity.getModifierLe());
        response.setModifierPar(entity.getModifierPar());
        return response;
    }
}
