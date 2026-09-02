package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Coefficient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoefficientRepository extends BaseRepository<Coefficient> {
    List<Coefficient> findByNiveauId(Long niveauId);

    Optional<Coefficient> findByNiveauIdAndMatiereId(Long niveauId, Long matiereId);
}
