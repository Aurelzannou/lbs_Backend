package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.PresenceProfesseur;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PresenceProfesseurRepository extends BaseRepository<PresenceProfesseur> {

    Optional<PresenceProfesseur> findByEmploiTempsIdAndDate(Long emploiTempsId, LocalDate date);
}
