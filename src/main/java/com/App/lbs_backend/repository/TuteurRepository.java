package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Tuteur;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TuteurRepository extends BaseRepository<Tuteur> {
    Optional<Tuteur> findByEmail(String email);
}
