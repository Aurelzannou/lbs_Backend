package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.EcheancierResponse;
import com.App.lbs_backend.entity.Echeancier;
import org.springframework.stereotype.Component;

@Component
public class EcheancierMapper implements Mapper<Echeancier, EcheancierResponse> {

    private final FraisScolaireMapper fraisScolaireMapper;

    public EcheancierMapper(FraisScolaireMapper fraisScolaireMapper) {
        this.fraisScolaireMapper = fraisScolaireMapper;
    }

    @Override
    public EcheancierResponse toResponse(Echeancier entity) {
        if (entity == null) return null;
        return new EcheancierResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getFraisScolaireId(),
                entity.getNumero(),
                entity.getLibelle(),
                entity.getDateEcheance(),
                entity.getMontant(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                fraisScolaireMapper.toResponse(entity.getFraisScolaire())
        );
    }
}