package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Niveau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NiveauRepository extends BaseRepository<Niveau> {

    @Override
    @Query("SELECT n FROM Niveau n WHERE lower(n.code) LIKE lower(concat('%', :filter, '%')) OR lower(n.libelle) LIKE lower(concat('%', :filter, '%'))")
    Page<Niveau> findByLabelContaining(@Param("filter") String filter, Pageable pageable);
}

