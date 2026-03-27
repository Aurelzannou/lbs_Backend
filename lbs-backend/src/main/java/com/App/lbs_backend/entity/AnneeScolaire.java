package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lbs_annee_scolaire", schema = "lbs")
@AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_ansc_modifier_le"))
public class AnneeScolaire extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_ansc_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_ansc_code", length = 20)
    private String code;

    @Column(name = "lbs_ansc_libelle", length = 100)
    private String libelle;

    @Column(name = "lbs_ansc_date_debut")
    private LocalDate dateDebut;

    @Column(name = "lbs_ansc_date_fin")
    private LocalDate dateFin;

    @Column(name = "lbs_ansc_actif")
    private Boolean actif;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public AnneeScolaire() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}