package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_periode_academique", schema = "lbs")
public class PeriodeAcademique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_peac_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_peac_code", length = 20)
    private String code;

    @Column(name = "lbs_peac_libelle", length = 150)
    private String libelle; // Ex: Trimestre 1, Semestre 1...

    @Column(name = "lbs_peac_annee_scolaire_id")
    private Integer anneeScolaireId;

    @Column(name = "lbs_peac_date_debut")
    private LocalDate dateDebut;

    @Column(name = "lbs_peac_date_fin")
    private LocalDate dateFin;

    @Column(name = "lbs_peac_verrouille")
    private Boolean verrouille; // Une fois verrouillé, on ne peut plus modifier les notes

    @Column(name = "lbs_peac_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_peac_modifier_par", length = 100)
    private String modifierPar;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_peac_annee_scolaire_id", insertable = false, updatable = false)
    private AnneeScolaire anneeScolaire;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
        if (this.verrouille == null) this.verrouille = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifierLe = LocalDateTime.now();
    }

    public PeriodeAcademique() {}

    // ===== GETTERS & SETTERS =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Integer getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Integer anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public Boolean getVerrouille() { return verrouille; }
    public void setVerrouille(Boolean verrouille) { this.verrouille = verrouille; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public AnneeScolaire getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) { this.anneeScolaire = anneeScolaire; }
}
