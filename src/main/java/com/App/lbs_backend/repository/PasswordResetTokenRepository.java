package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    /** Invalide les jetons encore actifs d'un compte — appelé avant d'en émettre un nouveau
        pour qu'un seul lien à la fois soit valable. */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.utilise = true WHERE t.keycloakId = :keycloakId AND t.utilise = false")
    void invaliderJetonsActifs(@Param("keycloakId") String keycloakId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expireLe < :avant")
    void supprimerExpires(@Param("avant") LocalDateTime avant);
}
