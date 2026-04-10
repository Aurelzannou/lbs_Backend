package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends BaseRepository<Utilisateur> {
    Optional<Utilisateur> findByKeycloack(String keycloakId);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);

    @Override
    @Query("SELECT u FROM Utilisateur u WHERE " +
           "LOWER(u.nom) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(u.prenom) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(u.login) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Utilisateur> findByLabelContaining(@Param("filter") String filter, Pageable pageable);
}