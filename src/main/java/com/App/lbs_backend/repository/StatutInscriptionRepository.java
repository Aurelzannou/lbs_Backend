package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.StatutInscription;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatutInscriptionRepository extends BaseRepository<StatutInscription> {
    Optional<StatutInscription> findByCode(String code);
}
