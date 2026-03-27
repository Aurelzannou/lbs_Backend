package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.DossierEtape;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierEtapeRepository extends BaseRepository<DossierEtape> {
    List<DossierEtape> findByDossierEleveId(Integer dossierEleveId);
}
