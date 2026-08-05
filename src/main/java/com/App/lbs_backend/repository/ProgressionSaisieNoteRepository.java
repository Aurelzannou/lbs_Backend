package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.ProgressionSaisieNote;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressionSaisieNoteRepository extends BaseRepository<ProgressionSaisieNote> {
    Optional<ProgressionSaisieNote> findByClasseIdAndMatiereIdAndPeriodeId(Long classeId, Long matiereId, Long periodeId);
}
