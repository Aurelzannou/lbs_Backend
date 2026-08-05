package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.ProgressionEtapeHistorique;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressionEtapeHistoriqueRepository extends BaseRepository<ProgressionEtapeHistorique> {
    List<ProgressionEtapeHistorique> findByProgressionIdOrderByDateTransitionAsc(Long progressionId);
}
