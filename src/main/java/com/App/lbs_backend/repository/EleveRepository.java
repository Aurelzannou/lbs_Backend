package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Eleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EleveRepository extends BaseRepository<Eleve> {

    @Override
    @Query("SELECT e FROM Eleve e WHERE " +
           "LOWER(e.nom) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Eleve> findByLabelContaining(@Param("filter") String filter, Pageable pageable);

    List<Eleve> findByTuteurId(Long tuteurId);

    @Query("""
        SELECT e FROM Eleve e
        WHERE (:classeId IS NULL OR e.classeId = :classeId)
          AND (:filter IS NULL OR :filter = ''
               OR LOWER(e.nom) LIKE LOWER(CONCAT('%', :filter, '%'))
               OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :filter, '%')))
        ORDER BY e.id DESC
        """)
    Page<Eleve> searchFiltered(
        @Param("classeId") Long classeId,
        @Param("filter")   String filter,
        Pageable pageable
    );
}
