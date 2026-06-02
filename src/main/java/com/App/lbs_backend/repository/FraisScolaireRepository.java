package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.FraisScolaire;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraisScolaireRepository extends BaseRepository<FraisScolaire> {

    List<FraisScolaire> findByClasseIdAndAnneeScolaireId(Long classeId, Long anneeScolaireId);
}