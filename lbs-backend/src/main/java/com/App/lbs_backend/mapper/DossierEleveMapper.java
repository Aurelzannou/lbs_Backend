package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import org.springframework.stereotype.Component;

@Component
public class DossierEleveMapper implements Mapper<DossierEleve, DossierEleveResponse> {

    private final ClasseMapper classeMapper;
    private final TypeOperationMapper typeOperationMapper;
    private final ActeMapper acteMapper;
    private final UtilisateurMapper utilisateurMapper;
    private final AnneeScolaireMapper anneeScolaireMapper;

    public DossierEleveMapper(ClasseMapper classeMapper,
                              TypeOperationMapper typeOperationMapper,
                              ActeMapper acteMapper,
                              UtilisateurMapper utilisateurMapper,
                              AnneeScolaireMapper anneeScolaireMapper) {
        this.classeMapper = classeMapper;
        this.typeOperationMapper = typeOperationMapper;
        this.acteMapper = acteMapper;
        this.utilisateurMapper = utilisateurMapper;
        this.anneeScolaireMapper = anneeScolaireMapper;
    }

    @Override
    public DossierEleveResponse toResponse(DossierEleve entity) {
        if (entity == null) return null;
        return new DossierEleveResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getNomEleve(),
                entity.getPrenomEleve(),
                entity.getSexe(),
                entity.getAge(),
                entity.getMatricule(),
                entity.getActif(),
                entity.getClasseId(),
                entity.getDateDebut(),
                entity.getDateFin(),
                entity.getSoufant(),
                entity.getNumero(),
                entity.getUtilisateurId(),
                entity.getProvenance(),
                entity.getTypeOperationId(),
                entity.getActeId(),
                entity.getPhoto(),
                entity.getAnneeScolaireId(),
                entity.getRemise(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                classeMapper.toResponse(entity.getClasse()),
                typeOperationMapper.toResponse(entity.getTypeOperation()),
                acteMapper.toResponse(entity.getActe()),
                utilisateurMapper.toResponse(entity.getUtilisateur()),
                anneeScolaireMapper.toResponse(entity.getAnneeScolaire())
        );
    }
}