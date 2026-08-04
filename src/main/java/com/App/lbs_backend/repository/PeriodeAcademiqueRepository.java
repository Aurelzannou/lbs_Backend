package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.PeriodeAcademique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PeriodeAcademiqueRepository extends BaseRepository<PeriodeAcademique> {
    /** Toutes les périodes d'une année scolaire, dans l'ordre chronologique — sert à calculer la
        moyenne annuelle (moyenne simple des moyennes des trimestres disponibles). */
    List<PeriodeAcademique> findByAnneeScolaireIdOrderByDateDebutAsc(Long anneeScolaireId);

    @Query("""
        SELECT p FROM PeriodeAcademique p
        WHERE (:anneeId IS NULL OR p.anneeScolaireId = :anneeId)
          AND (:filter IS NULL OR :filter = ''
               OR lower(p.code) LIKE lower(concat('%', :filter, '%'))
               OR lower(p.libelle) LIKE lower(concat('%', :filter, '%')))
        ORDER BY p.dateDebut ASC
        """)
    Page<PeriodeAcademique> searchFiltered(
        @Param("anneeId") Long anneeId,
        @Param("filter") String filter,
        Pageable pageable
    );

    /** Périodes de la même année scolaire dont l'intervalle [dateDebut, dateFin] chevauche celui
        donné — sert à empêcher deux périodes de se superposer dans le temps. */
    @Query("""
        SELECT p FROM PeriodeAcademique p
        WHERE p.anneeScolaireId = :anneeScolaireId
          AND (:excludeId IS NULL OR p.id <> :excludeId)
          AND p.dateDebut <= :dateFin
          AND p.dateFin >= :dateDebut
        """)
    List<PeriodeAcademique> findChevauchantes(
        @Param("anneeScolaireId") Long anneeScolaireId,
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin,
        @Param("excludeId") Long excludeId
    );
}
