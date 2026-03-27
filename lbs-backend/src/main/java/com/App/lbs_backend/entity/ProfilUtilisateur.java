package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lbs_profil_utilisateur", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_prut_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_prut_modifier_par", length = 100))
})
public class ProfilUtilisateur extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_prut_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prut_profil_id")
    private Integer profilId;

    @Column(name = "lbs_prut_code", unique = true)
    private String code;

    @Column(name = "lbs_prut_utilisateur_id")
    private Integer utilisateurId;

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Integer getProfilId() { return profilId; }
    public void setProfilId(Integer profilId) { this.profilId = profilId; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Profil getProfil() { return profil; }
    public void setProfil(Profil profil) { this.profil = profil; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}