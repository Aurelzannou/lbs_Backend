package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Echeancier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcheancierRepository extends BaseRepository<Echeancier> {

    List<Echeancier> findByFraisScolaireIdOrderByNumeroAsc(Long fraisScolaireId);
}