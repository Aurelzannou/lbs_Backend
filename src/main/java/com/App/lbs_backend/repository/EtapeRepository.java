package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Etape;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtapeRepository extends BaseRepository<Etape> {
    Optional<Etape> findByCode(String code);
}
