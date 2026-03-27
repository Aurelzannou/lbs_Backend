package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import org.springframework.stereotype.Component;

@Component
public class DossierEleveMapper {

    public DossierEleveResponse toResponse(DossierEleve entity) {
        if (entity == null) return null;
        
        DossierEleveResponse response = new DossierEleveResponse();
        response.setId(entity.getId());
        response.setUuid(entity.getUuid());
        response.setCode(entity.getCode());
        response.setEleveId(entity.getEleveId());
        if (entity.getEleve() != null) {
            response.setEleveNom(entity.getEleve().getNom());
            response.setElevePrenom(entity.getEleve().getPrenom());
        }
        response.setClasseId(entity.getClasseId());
        if (entity.getClasse() != null) response.setClasseLibelle(entity.getClasse().getLibelle());
        response.setAnneeScolaireId(entity.getAnneeScolaireId());
        if (entity.getAnneeScolaire() != null) response.setAnneeScolaireLibelle(entity.getAnneeScolaire().getLibelle());
        response.setDateDebut(entity.getDateDebut());
        response.setDateFin(entity.getDateFin());
        response.setStatutId(entity.getStatutId());
        if (entity.getStatut() != null) response.setStatutLibelle(entity.getStatut().getLibelle());
        response.setEtapeCouranteId(entity.getEtapeCouranteId());
        if (entity.getEtapeCourante() != null) response.setEtapeCouranteLibelle(entity.getEtapeCourante().getLibelle());
        response.setRemise(entity.getRemise());
        response.setNumero(entity.getNumero());
        response.setTypeOperationId(entity.getTypeOperationId());
        if (entity.getTypeOperation() != null) response.setTypeOperationLibelle(entity.getTypeOperation().getLibelle());
        response.setActeId(entity.getActeId());
        response.setUtilisateurId(entity.getUtilisateurId());
        
        return response;
    }
}
