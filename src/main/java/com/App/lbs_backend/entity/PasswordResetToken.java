package com.App.lbs_backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Jeton à usage unique pour la réinitialisation de mot de passe (« Mot de passe oublié »).
 * Fonctionne pour n'importe quel type de compte (admin, tuteur, professeur) car il est
 * rattaché à l'identifiant Keycloak, pas à une entité métier précise. Le mot de passe n'est
 * jamais transmis par email : l'utilisateur le choisit lui-même sur la page de réinitialisation.
 */
@Entity
@Table(name = "lbs_password_reset_token", schema = "lbs")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prt_token", length = 100, unique = true, nullable = false)
    private String token;

    @Column(name = "prt_keycloak_id", length = 100, nullable = false)
    private String keycloakId;

    /** Email/login ciblé — conservé pour le journal et pour reconstruire le contexte de l'email. */
    @Column(name = "prt_email", length = 150, nullable = false)
    private String email;

    @Column(name = "prt_expire_le", nullable = false)
    private LocalDateTime expireLe;

    @Column(name = "prt_utilise", nullable = false)
    private boolean utilise = false;

    @Column(name = "prt_cree_le", nullable = false)
    private LocalDateTime creeLe = LocalDateTime.now();

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String keycloakId, String email, LocalDateTime expireLe) {
        this.token = token;
        this.keycloakId = keycloakId;
        this.email = email;
        this.expireLe = expireLe;
    }

    public boolean estValide() {
        return !utilise && expireLe != null && expireLe.isAfter(LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getExpireLe() { return expireLe; }
    public void setExpireLe(LocalDateTime expireLe) { this.expireLe = expireLe; }

    public boolean isUtilise() { return utilise; }
    public void setUtilise(boolean utilise) { this.utilise = utilise; }

    public LocalDateTime getCreeLe() { return creeLe; }
    public void setCreeLe(LocalDateTime creeLe) { this.creeLe = creeLe; }
}
