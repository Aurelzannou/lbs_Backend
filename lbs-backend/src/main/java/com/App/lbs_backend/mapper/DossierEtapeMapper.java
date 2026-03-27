package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.request.DossierEtapeRequest;
import com.App.lbs_backend.dto.response.DossierEtapeResponse;
import com.App.lbs_backend.entity.DossierEtape;
import org.springframework.stereotype.Component;

@Component
public class DossierEtapeMapper {

    public DossierEtape toEntity(DossierEtapeRequest request) {
        if (request == null) return null;
        DossierEtape entity = new DossierEtape();
        entity.setDossierEleveId(request.getDossierEleveId());
        entity.setEtapeId(request.getEtapeId());
        entity.setStatut(request.getStatut());
        entity.setCommentaire(request.getCommentaire());
        entity.setUtilisateurId(request.getUtilisateurId());
        return entity;
    }

    public void updateEntityFromRequest(DossierEtapeRequest request, DossierEtape entity) {
        if (request == null || entity == null) return;
        if (request.getDossierEleveId() != null) entity.setDossierEleveId(request.getDossierEleveId());
        if (request.getEtapeId() != null) entity.setEtapeId(request.getEtapeId());
        if (request.getStatut() != null) entity.setStatut(request.getStatut());
        if (request.getCommentaire() != null) entity.setCommentaire(request.getCommentaire());
        if (request.getUtilisateurId() != null) entity.setUtilisateurId(request.getUtilisateurId());
    }

    public DossierEtapeResponse toResponse(DossierEtape entity) {
        if (entity == null) return null;
        DossierEtapeResponse response = new DossierEtapeResponse();
        response.setId(entity.getId());
        response.setUuid(entity.getUuid());
        response.setDossierEleveId(entity.getDossierEleveId());
        response.setEtapeId(entity.getEtapeId());
        if (entity.getEtape() != null) {
            response.setEtapeLibelle(entity.getEtape().getLibelle());
        }
        response.setStatut(entity.getStatut());
        response.setDateTraitement(entity.getDateTraitement());
        response.setCommentaire(entity.getCommentaire());
        response.setUtilisateurId(entity.getUtilisateurId());
        if (entity.getUtilisateur() != null) {
            response.setUtilisateurNom(entity.getUtilisateur().getNom() + " " + entity.getUtilisateur().getPrenom());
        }
        response.setModifierLe(entity.getModifierLe());
        response.setModifierPar(entity.getModifierPar());
        return response;
    }
}
