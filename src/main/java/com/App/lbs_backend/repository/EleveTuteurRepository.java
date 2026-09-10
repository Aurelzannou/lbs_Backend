package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.EleveTuteur;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EleveTuteurRepository extends BaseRepository<EleveTuteur> {
    List<EleveTuteur> findByEleveId(Long eleveId);
    List<EleveTuteur> findByTuteurId(Long tuteurId);
    Optional<EleveTuteur> findByEleveIdAndTuteurId(Long eleveId, Long tuteurId);
}
