package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_profil_utilisateur", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_prut_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_prut_modifier_par", length = 100))
})
public class ProfilUtilisateur extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_prut_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prut_profil_id")
    private Long profilId;

    @Column(name = "lbs_prut_code", unique = true)
    private String code;

    @Column(name = "lbs_prut_utilisateur_id")
    private Long utilisateurId;

    @Column(name = "lbs_prut_token", length = 500)
    private String token;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_prut_profil_id", insertable = false, updatable = false)
    private Profil profil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_prut_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public ProfilUtilisateur() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Long getProfilId() { return profilId; }
    public void setProfilId(Long profilId) { this.profilId = profilId; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Profil getProfil() { return profil; }
    public void setProfil(Profil profil) { this.profil = profil; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}