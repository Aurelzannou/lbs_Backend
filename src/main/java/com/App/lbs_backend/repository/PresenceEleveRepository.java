package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.PresenceEleve;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresenceEleveRepository extends BaseRepository<PresenceEleve> {

    @EntityGraph(attributePaths = {"eleve"})
    List<PresenceEleve> findByEmploiTempsIdAndDate(Long emploiTempsId, LocalDate date);

    Optional<PresenceEleve> findByEmploiTempsIdAndDateAndEleveId(Long emploiTempsId, LocalDate date, Long eleveId);

    @EntityGraph(attributePaths = {"emploiDuTemps", "emploiDuTemps.matiere"})
    List<PresenceEleve> findByEleveIdOrderByDateDesc(Long eleveId);

    /** Nombre d'absences d'un élève sur une période donnée — sert à la suggestion automatique de
        note de conduite (18 - nombre d'absences). */
    long countByEleveIdAndDateBetweenAndStatut(Long eleveId, LocalDate dateDebut, LocalDate dateFin, String statut);
}
