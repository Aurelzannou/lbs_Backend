package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.ValidationBulletin;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationBulletinRepository extends BaseRepository<ValidationBulletin> {
    Optional<ValidationBulletin> findByClasseIdAndPeriodeId(Long classeId, Long periodeId);
    List<ValidationBulletin> findByPeriodeIdAndAnneeScolaireId(Long periodeId, Long anneeScolaireId);
}
