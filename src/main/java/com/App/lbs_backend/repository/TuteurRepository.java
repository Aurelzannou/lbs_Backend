package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Tuteur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TuteurRepository extends BaseRepository<Tuteur> {
    Optional<Tuteur> findByEmail(String email);

    @Query("""
        SELECT t FROM Tuteur t
        WHERE (:filter IS NULL OR :filter = ''
               OR LOWER(t.nom) LIKE LOWER(CONCAT('%', :filter, '%'))
               OR LOWER(t.prenom) LIKE LOWER(CONCAT('%', :filter, '%'))
               OR LOWER(t.email) LIKE LOWER(CONCAT('%', :filter, '%'))
               OR t.telephone1 LIKE CONCAT('%', :filter, '%'))
        ORDER BY t.nom ASC, t.prenom ASC
        """)
    Page<Tuteur> searchFiltered(@Param("filter") String filter, Pageable pageable);
}
