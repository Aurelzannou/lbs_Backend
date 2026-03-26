package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Utilisateur;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends BaseRepository<Utilisateur> {
    Optional<Utilisateur> findByKeycloack(String keycloakId);
}