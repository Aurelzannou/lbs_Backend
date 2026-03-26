package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Coefficient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoefficientRepository extends BaseRepository<Coefficient> {
    List<Coefficient> findByNiveauId(Integer niveauId);
}
