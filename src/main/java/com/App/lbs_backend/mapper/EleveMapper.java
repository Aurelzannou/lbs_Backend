package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.EleveResponse;
import com.App.lbs_backend.entity.Eleve;
import org.springframework.stereotype.Component;

@Component
public class EleveMapper implements Mapper<Eleve, EleveResponse> {

    private final UtilisateurMapper utilisateurMapper;
    private final ClasseMapper classeMapper;

    public EleveMapper(UtilisateurMapper utilisateurMapper, ClasseMapper classeMapper) {
        this.utilisateurMapper = utilisateurMapper;
        this.classeMapper = classeMapper;
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
                entity.getActif(),
                entity.getSouffrant(),
                entity.getProvenance(),
                entity.getPhoto(),
                entity.getUtilisateurId(),
                entity.getClasseId(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                utilisateurMapper.toResponse(entity.getUtilisateur()),
                classeMapper.toResponse(entity.getClasse())
        );
    }
}
