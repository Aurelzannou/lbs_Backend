package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Professeur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfesseurRepository extends BaseRepository<Professeur> {

    // Un professeur n'a pas de matricule saisi par l'utilisateur (code auto-généré, non
    // significatif) — la recherche générique doit donc porter sur nom/prénom, pas sur le code.
    @Override
    @Query("SELECT p FROM Professeur p WHERE " +
           "LOWER(p.nom) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Professeur> findByLabelContaining(@Param("filter") String filter, Pageable pageable);

    /** Résout le professeur connecté au portail à partir de l'email du JWT (login Keycloak). */
    Optional<Professeur> findByEmail(String email);

    /** Résout le professeur à partir du jeton d'activation contenu dans le lien envoyé par email. */
    Optional<Professeur> findByActivationToken(String activationToken);

    /** Résout le professeur rattaché à un compte de connexion Keycloak donné — sert à détecter
        qu'un email/compte est déjà utilisé par une autre fiche avant d'y relier un nouveau
        professeur (la colonne keycloak_id est unique). */
    Optional<Professeur> findByKeycloakId(String keycloakId);
}