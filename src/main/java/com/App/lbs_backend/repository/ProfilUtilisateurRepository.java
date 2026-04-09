package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.ProfilUtilisateur;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfilUtilisateurRepository extends BaseRepository<ProfilUtilisateur> {
    List<ProfilUtilisateur> findByUtilisateurId(Long utilisateurId);
}