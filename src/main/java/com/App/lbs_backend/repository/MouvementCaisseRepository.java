package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.MouvementCaisse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MouvementCaisseRepository extends BaseRepository<MouvementCaisse> {

    List<MouvementCaisse> findByCaisseIdOrderByDateMouvementDesc(Long caisseId);
}
