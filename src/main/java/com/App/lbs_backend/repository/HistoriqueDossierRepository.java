package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.HistoriqueDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueDossierRepository extends JpaRepository<HistoriqueDossier, Long> {
    List<HistoriqueDossier> findByDossierIdOrderByEffectueLeDesc(Long dossierId);
}
