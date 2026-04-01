package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.DossierEtape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierEtapeRepository extends JpaRepository<DossierEtape, Long>, JpaSpecificationExecutor<DossierEtape> {
    List<DossierEtape> findByDossierEleveId(Long dossierEleveId);
    Optional<DossierEtape> findByUuid(String uuid);
}
