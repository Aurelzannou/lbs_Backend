package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.EmploiDuTemps;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface EmploiDuTempsRepository extends BaseRepository<EmploiDuTemps> {

    @EntityGraph(attributePaths = {"classe", "matiere", "professeur", "anneeScolaire"})
    List<EmploiDuTemps> findByClasseIdAndAnneeScolaireIdOrderByJourAscHeureDebutAsc(Long classeId, Long anneeScolaireId);

    @EntityGraph(attributePaths = {"classe", "matiere", "professeur", "anneeScolaire"})
    List<EmploiDuTemps> findByClasseIdAndAnneeScolaireIdAndJourOrderByHeureDebutAsc(Long classeId, Long anneeScolaireId, String jour);

    long countByProfId(Long profId);

    /** Couples distincts (classe, matière) qu'un professeur enseigne pour une année scolaire —
        alimente son "Mes classes à noter" côté portail, et sert de base au contrôle d'accès
        (un professeur ne peut noter que ce qu'il enseigne réellement). */
    @Query("""
        SELECT DISTINCT e.classeId AS classeId, e.matiereId AS matiereId
        FROM EmploiDuTemps e
        WHERE e.profId = :profId AND e.anneeScolaireId = :anneeScolaireId
        """)
    List<ClasseMatiereProjection> findDistinctClasseMatiereByProfIdAndAnneeScolaireId(
        @Param("profId") Long profId,
        @Param("anneeScolaireId") Long anneeScolaireId
    );

    interface ClasseMatiereProjection {
        Long getClasseId();
        Long getMatiereId();
    }

    @Query("""
        SELECT COUNT(e) > 0 FROM EmploiDuTemps e
        WHERE e.profId = :profId
          AND e.anneeScolaireId = :anneeScolaireId
          AND e.jour = :jour
          AND e.heureDebut < :heureFin
          AND e.heureFin > :heureDebut
          AND (:excludeId IS NULL OR e.id <> :excludeId)
        """)
    boolean existsConflitProfesseur(
        @Param("profId") Long profId,
        @Param("anneeScolaireId") Long anneeScolaireId,
        @Param("jour") String jour,
        @Param("heureDebut") LocalTime heureDebut,
        @Param("heureFin") LocalTime heureFin,
        @Param("excludeId") Long excludeId
    );

    @Query("""
        SELECT COUNT(e) > 0 FROM EmploiDuTemps e
        WHERE e.classeId = :classeId
          AND e.anneeScolaireId = :anneeScolaireId
          AND e.jour = :jour
          AND e.heureDebut < :heureFin
          AND e.heureFin > :heureDebut
          AND (:excludeId IS NULL OR e.id <> :excludeId)
        """)
    boolean existsConflitClasse(
        @Param("classeId") Long classeId,
        @Param("anneeScolaireId") Long anneeScolaireId,
        @Param("jour") String jour,
        @Param("heureDebut") LocalTime heureDebut,
        @Param("heureFin") LocalTime heureFin,
        @Param("excludeId") Long excludeId
    );

    /**
     * Variantes utilisées lors de la réorganisation d'un jour entier (glisser-déposer) : on
     * exclut TOUT le lot de cours en cours de réorganisation, pas seulement le cours courant,
     * pour éviter des faux conflits transitoires entre cours du même lot qui échangent leurs
     * créneaux entre eux.
     */
    @Query("""
        SELECT COUNT(e) > 0 FROM EmploiDuTemps e
        WHERE e.profId = :profId
          AND e.anneeScolaireId = :anneeScolaireId
          AND e.jour = :jour
          AND e.heureDebut < :heureFin
          AND e.heureFin > :heureDebut
          AND e.id NOT IN :excludeIds
        """)
    boolean existsConflitProfesseurHorsLot(
        @Param("profId") Long profId,
        @Param("anneeScolaireId") Long anneeScolaireId,
        @Param("jour") String jour,
        @Param("heureDebut") LocalTime heureDebut,
        @Param("heureFin") LocalTime heureFin,
        @Param("excludeIds") List<Long> excludeIds
    );

    @Query("""
        SELECT COUNT(e) > 0 FROM EmploiDuTemps e
        WHERE e.classeId = :classeId
          AND e.anneeScolaireId = :anneeScolaireId
          AND e.jour = :jour
          AND e.heureDebut < :heureFin
          AND e.heureFin > :heureDebut
          AND e.id NOT IN :excludeIds
        """)
    boolean existsConflitClasseHorsLot(
        @Param("classeId") Long classeId,
        @Param("anneeScolaireId") Long anneeScolaireId,
        @Param("jour") String jour,
        @Param("heureDebut") LocalTime heureDebut,
        @Param("heureFin") LocalTime heureFin,
        @Param("excludeIds") List<Long> excludeIds
    );
}
