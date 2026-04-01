package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_utilisateur", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_util_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_util_modifier_par", length = 100))
})
public class Utilisateur extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_util_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_util_code", length = 20)
    private String code;

    @Column(name = "lbs_util_nom", length = 100)
    private String nom;

    @Column(name = "lbs_util_prenom", length = 100)
    private String prenom;

    @Column(name = "lbs_util_login", length = 50)
    private String login;

    @Column(name = "lbs_util_photo", length = 500)
    private String photo;

    @Column(name = "lbs_util_sexe", length = 10)
    private String sexe;

    @Column(name = "lbs_util_keycloack", length = 200)
    private String keycloack;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Utilisateur() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public String getKeycloack() { return keycloack; }
    public void setKeycloack(String keycloack) { this.keycloack = keycloack; }
}