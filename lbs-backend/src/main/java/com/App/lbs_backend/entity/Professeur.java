package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_professeur", schema = "lbs")
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_prfs_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prfs_code", length = 20)
    private String code;

    @Column(name = "lbs_prfs_nom", length = 100)
    private String nom;

    @Column(name = "lbs_prfs_prenom", length = 100)
    private String prenom;

    @Column(name = "lbs_prfs_email", length = 150)
    private String email;

    @Column(name = "lbs_prfs_residence", length = 200)
    private String residence;

    @Column(name = "lbs_prfs_num", length = 30)
    private String num;

    @Column(name = "lbs_prfs_actif")
    private Boolean actif;

    @Column(name = "lbs_prfs_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_prfs_modifier_par", length = 100)
    private String modifierPar;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifierLe = LocalDateTime.now(); }

    public Professeur() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getResidence() { return residence; }
    public void setResidence(String residence) { this.residence = residence; }

    public String getNum() { return num; }
    public void setNum(String num) { this.num = num; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }
}