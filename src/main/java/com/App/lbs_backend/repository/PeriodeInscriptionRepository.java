package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.PeriodeInscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Repository
public interface PeriodeInscriptionRepository extends BaseRepository<PeriodeInscription> {

    // PeriodeInscription n'a pas de champ "code" — on surcharge toutes les méthodes qui l'utilisent

    @Override
    @Query("SELECT p FROM PeriodeInscription p WHERE lower(p.libelle) like LOWER(concat('%', :filter, '%'))")
    Page<PeriodeInscription> findByLabelContaining(@Param("filter") String filter, Pageable pageable);

    @Override
    @Query("SELECT p FROM PeriodeInscription p WHERE p.libelle = :code")
    Optional<PeriodeInscription> findByCode(@Param("code") String code);

    @Override
    @Query("SELECT p FROM PeriodeInscription p WHERE upper(p.libelle) = upper(:code)")
    Optional<PeriodeInscription> findByStrictCode(@Param("code") String code);

    @Override
    @Query("SELECT COUNT(p) > 0 FROM PeriodeInscription p WHERE p.libelle = :code")
    boolean existsByCode(@Param("code") String code);

    @Override
    @Query("SELECT p FROM PeriodeInscription p WHERE p.libelle IN :codes")
    Page<PeriodeInscription> findByCodeIn(@Param("codes") java.util.Collection<String> codes, Pageable pageable);

    @Query("SELECT p FROM PeriodeInscription p WHERE p.anneeScolaireId = :anneeId AND p.actif = true AND p.dateOuverture <= :today AND p.dateCloture >= :today")
    Optional<PeriodeInscription> findPeriodeActive(@Param("anneeId") Long anneeId, @Param("today") LocalDate today);
}
