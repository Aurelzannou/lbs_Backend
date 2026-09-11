package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.MouvementCaisse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouvementCaisseRepository extends BaseRepository<MouvementCaisse> {

    List<MouvementCaisse> findByCaisseIdOrderByDateMouvementDesc(Long caisseId);

    /** Mouvements d'une caisse survenus dans un intervalle de dates — sert à restreindre le
        journal à une année scolaire (bornes = dateDebut/dateFin de l'année, `fin` exclusive). */
    @Query("""
        SELECT m FROM MouvementCaisse m
        WHERE m.caisseId = :caisseId
          AND (:debut IS NULL OR m.dateMouvement >= :debut)
          AND (:fin IS NULL OR m.dateMouvement < :fin)
        ORDER BY m.dateMouvement DESC
        """)
    List<MouvementCaisse> findByCaisseIdAndPeriode(
        @Param("caisseId") Long caisseId,
        @Param("debut") LocalDateTime debut,
        @Param("fin") LocalDateTime fin
    );
}
