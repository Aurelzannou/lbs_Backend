package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.AnneeScolaire;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnneeScolaireRepository extends BaseRepository<AnneeScolaire> {

    /** Années scolaires dont l'intervalle [dateDebut, dateFin] chevauche celui donné — deux années
        scolaires ne peuvent jamais se superposer dans le temps (un jour donné appartient à une seule
        année). */
    @Query("""
        SELECT a FROM AnneeScolaire a
        WHERE (:excludeId IS NULL OR a.id <> :excludeId)
          AND a.dateDebut <= :dateFin
          AND a.dateFin >= :dateDebut
        """)
    List<AnneeScolaire> findChevauchantes(
        @Param("dateDebut") LocalDate dateDebut,
        @Param("dateFin") LocalDate dateFin,
        @Param("excludeId") Long excludeId
    );

    /** Désactive toutes les autres années scolaires — une seule peut être active à la fois. */
    @Modifying
    @Transactional
    @Query("UPDATE AnneeScolaire a SET a.actif = false WHERE a.id <> :excludeId AND a.actif = true")
    void desactiverAutres(@Param("excludeId") Long excludeId);
}