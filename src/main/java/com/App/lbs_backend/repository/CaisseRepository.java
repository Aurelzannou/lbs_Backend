package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Caisse;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaisseRepository extends BaseRepository<Caisse> {

    /** Caisse(s) rattachée(s) à un utilisateur (caissier). */
    List<Caisse> findByUtilisateurId(Long utilisateurId);
}