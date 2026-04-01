package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.EleveTuteur;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EleveTuteurRepository extends BaseRepository<EleveTuteur> {
    List<EleveTuteur> findByEleveId(Long eleveId);
    List<EleveTuteur> findByTuteurId(Long tuteurId);
}
