package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.PeriodeAcademiqueResponse;
import com.App.lbs_backend.entity.PeriodeAcademique;
import org.springframework.stereotype.Component;

@Component
public class PeriodeAcademiqueMapper implements Mapper<PeriodeAcademique, PeriodeAcademiqueResponse> {

    private final AnneeScolaireMapper anneeScolaireMapper;

    public PeriodeAcademiqueMapper(AnneeScolaireMapper anneeScolaireMapper) {
        this.anneeScolaireMapper = anneeScolaireMapper;
    }

    @Override
    public PeriodeAcademiqueResponse toResponse(PeriodeAcademique entity) {
        if (entity == null) return null;
        return new PeriodeAcademiqueResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getAnneeScolaireId(),
                entity.getDateDebut(),
                entity.getDateFin(),
                entity.getVerrouille(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                anneeScolaireMapper.toResponse(entity.getAnneeScolaire())
        );
    }
}
