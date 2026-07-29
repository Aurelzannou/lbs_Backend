package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.EmploiDuTempsResponse;
import com.App.lbs_backend.entity.EmploiDuTemps;
import org.springframework.stereotype.Component;

@Component
public class EmploiDuTempsMapper implements Mapper<EmploiDuTemps, EmploiDuTempsResponse> {

    @Override
    public EmploiDuTempsResponse toResponse(EmploiDuTemps entity) {
        if (entity == null) return null;

        EmploiDuTempsResponse response = new EmploiDuTempsResponse();
        response.setId(entity.getId());
        response.setUuid(entity.getUuid());
        response.setCode(entity.getCode());
        response.setClasseId(entity.getClasseId());
        if (entity.getClasse() != null) response.setClasseLibelle(entity.getClasse().getLibelle());
        response.setAnneeScolaireId(entity.getAnneeScolaireId());
        if (entity.getAnneeScolaire() != null) response.setAnneeScolaireLibelle(entity.getAnneeScolaire().getLibelle());
        response.setMatiereId(entity.getMatiereId());
        if (entity.getMatiere() != null) response.setMatiereLibelle(entity.getMatiere().getLibelle());
        response.setProfId(entity.getProfId());
        if (entity.getProfesseur() != null) {
            response.setProfNomComplet(entity.getProfesseur().getNom() + " " + entity.getProfesseur().getPrenom());
            response.setProfActif(entity.getProfesseur().getActif());
        }
        response.setJour(entity.getJour());
        response.setHeureDebut(entity.getHeureDebut());
        response.setHeureFin(entity.getHeureFin());

        return response;
    }
}
