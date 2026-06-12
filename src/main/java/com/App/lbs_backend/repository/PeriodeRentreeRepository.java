package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.PeriodeRentree;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PeriodeRentreeRepository extends BaseRepository<PeriodeRentree> {

    // PeriodeRentree n'a pas de champ "code" — on surcharge toutes les méthodes qui l'utilisent

    @Override
    @Query("SELECT p FROM PeriodeRentree p WHERE lower(p.libelle) like LOWER(concat('%', :filter, '%'))")
    Page<PeriodeRentree> findByLabelContaining(@Param("filter") String filter, Pageable pageable);

    @Override
    @Query("SELECT p FROM PeriodeRentree p WHERE p.libelle = :code")
    Optional<PeriodeRentree> findByCode(@Param("code") String code);

    @Override
    @Query("SELECT p FROM PeriodeRentree p WHERE upper(p.libelle) = upper(:code)")
    Optional<PeriodeRentree> findByStrictCode(@Param("code") String code);

    @Override
    @Query("SELECT COUNT(p) > 0 FROM PeriodeRentree p WHERE p.libelle = :code")
    boolean existsByCode(@Param("code") String code);

    @Override
    @Query("SELECT p FROM PeriodeRentree p WHERE p.libelle IN :codes")
    Page<PeriodeRentree> findByCodeIn(@Param("codes") java.util.Collection<String> codes, Pageable pageable);

    @Query("SELECT p FROM PeriodeRentree p WHERE p.anneeScolaireId = :anneeId AND p.actif = true AND p.dateOuverture <= :today AND p.dateCloture >= :today")
    Optional<PeriodeRentree> findPeriodeActive(@Param("anneeId") Long anneeId, @Param("today") LocalDate today);
}
