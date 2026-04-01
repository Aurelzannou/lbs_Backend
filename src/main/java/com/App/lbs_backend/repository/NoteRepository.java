package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Note;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends BaseRepository<Note> {
    List<Note> findByDossierEleveId(Long dossierEleveId);
    List<Note> findByDossierEleveIdAndPeriodeId(Long dossierEleveId, Long periodeId);
}
