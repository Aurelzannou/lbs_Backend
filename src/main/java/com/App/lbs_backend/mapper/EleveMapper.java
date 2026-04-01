package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.EleveResponse;
import com.App.lbs_backend.entity.Eleve;
import org.springframework.stereotype.Component;

@Component
public class EleveMapper implements Mapper<Eleve, EleveResponse> {

    private final UtilisateurMapper utilisateurMapper;

    public EleveMapper(UtilisateurMapper utilisateurMapper) {
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    public EleveResponse toResponse(Eleve entity) {
        if (entity == null) return null;
        return new EleveResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getSexe(),
                entity.getDateNaissance(),
                entity.getMatricule(),
                entity.getActif(),
                entity.getSouffrant(),
                entity.getProvenance(),
                entity.getPhoto(),
                entity.getUtilisateurId(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                utilisateurMapper.toResponse(entity.getUtilisateur())
        );
    }
}
