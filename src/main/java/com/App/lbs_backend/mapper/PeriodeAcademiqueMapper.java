package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.PeriodeAcademiqueResponse;
import com.App.lbs_backend.entity.PeriodeAcademique;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PeriodeAcademiqueMapper implements Mapper<PeriodeAcademique, PeriodeAcademiqueResponse> {

    public static final String A_VENIR = "A_VENIR";
    public static final String EN_COURS = "EN_COURS";
    public static final String TERMINEE = "TERMINEE";

    private final AnneeScolaireMapper anneeScolaireMapper;

    public PeriodeAcademiqueMapper(AnneeScolaireMapper anneeScolaireMapper) {
        this.anneeScolaireMapper = anneeScolaireMapper;
    }

    /** Statut dérivé des dates de la période (jamais stocké) : permet d'avertir l'utilisateur
        qu'une période touche à sa fin, et sert aussi à limiter la saisie du professeur à la
        période en cours. */
    public static String calculerStatut(PeriodeAcademique entity) {
        if (entity.getDateDebut() == null || entity.getDateFin() == null) return null;
        LocalDate today = LocalDate.now();
        if (today.isBefore(entity.getDateDebut())) return A_VENIR;
        if (today.isAfter(entity.getDateFin())) return TERMINEE;
        return EN_COURS;
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
                anneeScolaireMapper.toResponse(entity.getAnneeScolaire()),
                calculerStatut(entity)
        );
    }
}
