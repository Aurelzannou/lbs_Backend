package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Note;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends BaseRepository<Note> {
    List<Note> findByInscriptionId(Integer inscriptionId);
    List<Note> findByInscriptionIdAndPeriodeId(Integer inscriptionId, Integer periodeId);
}
