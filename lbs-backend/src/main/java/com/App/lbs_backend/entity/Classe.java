package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_classe", schema = "lbs")
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_clas_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_clas_prof_id")
    private Integer profId;

    @Column(name = "lbs_clas_code", length = 20)
    private String code;

    @Column(name = "lbs_clas_libelle", length = 100)
    private String libelle;

    @Column(name = "lbs_clas_actif")
    private Boolean actif;

    @Column(name = "lbs_clas_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_clas_modifier_par", length = 100)
    private String modifierPar;

    // ===== RELATION =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_clas_prof_id", insertable = false, updatable = false)
    private Professeur professeur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifierLe = LocalDateTime.now(); }

    public Classe() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Integer getProfId() { return profId; }
    public void setProfId(Integer profId) { this.profId = profId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }
}