package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Profil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfilRepository extends BaseRepository<Profil> {
    
    Optional<Profil> findByCode(String code);

    @Override
    @Query("SELECT p FROM Profil p WHERE " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(p.libelle) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Profil> findByLabelContaining(@Param("filter") String filter, Pageable pageable);
}