package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lbs_classe", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_clas_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_clas_modifier_par", length = 100))
})
public class Classe extends AuditableEntity {

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

    @Column(name = "lbs_clas_niveau_id")
    private Integer niveauId;

    @Column(name = "lbs_clas_capacite_max")
    private Integer capaciteMax;

    @Column(name = "lbs_clas_actif")
    private Boolean actif;

    // ===== RELATION =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_clas_prof_id", insertable = false, updatable = false)
    private Professeur professeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_clas_niveau_id", insertable = false, updatable = false)
    private Niveau niveau;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

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

    public Integer getNiveauId() { return niveauId; }
    public void setNiveauId(Integer niveauId) { this.niveauId = niveauId; }

    public Integer getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(Integer capaciteMax) { this.capaciteMax = capaciteMax; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }

    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
}