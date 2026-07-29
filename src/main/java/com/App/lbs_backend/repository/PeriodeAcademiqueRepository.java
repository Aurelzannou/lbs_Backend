package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.PeriodeAcademique;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodeAcademiqueRepository extends BaseRepository<PeriodeAcademique> {
    /** Toutes les périodes d'une année scolaire, dans l'ordre chronologique — sert à calculer la
        moyenne annuelle (moyenne simple des moyennes des trimestres disponibles). */
    List<PeriodeAcademique> findByAnneeScolaireIdOrderByDateDebutAsc(Long anneeScolaireId);
}
