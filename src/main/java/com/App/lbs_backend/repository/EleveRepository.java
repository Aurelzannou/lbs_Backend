package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Eleve;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EleveRepository extends BaseRepository<Eleve> {

    @Override
    @Query("SELECT e FROM Eleve e WHERE " +
           "LOWER(e.nom) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Eleve> findByLabelContaining(@Param("filter") String filter, Pageable pageable);
}
