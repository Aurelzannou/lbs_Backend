package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.FraisScolaireResponse;
import com.App.lbs_backend.entity.FraisScolaire;
import org.springframework.stereotype.Component;

@Component
public class FraisScolaireMapper implements Mapper<FraisScolaire, FraisScolaireResponse> {

    private final AnneeScolaireMapper anneeScolaireMapper;
    private final ClasseMapper classeMapper;
    private final TypeFraisMapper typeFraisMapper;

    public FraisScolaireMapper(AnneeScolaireMapper anneeScolaireMapper,
                               ClasseMapper classeMapper,
                               TypeFraisMapper typeFraisMapper) {
        this.anneeScolaireMapper = anneeScolaireMapper;
        this.classeMapper = classeMapper;
        this.typeFraisMapper = typeFraisMapper;
    }

    @Override
    public FraisScolaireResponse toResponse(FraisScolaire entity) {
        if (entity == null) return null;
        return new FraisScolaireResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getAnneeScolaireId(),
                entity.getClasseId(),
                entity.getTypeFraisId(),
                entity.getMontant(),
                entity.getActif(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                anneeScolaireMapper.toResponse(entity.getAnneeScolaire()),
                classeMapper.toResponse(entity.getClasse()),
                typeFraisMapper.toResponse(entity.getTypeFrais())
        );
    }
}