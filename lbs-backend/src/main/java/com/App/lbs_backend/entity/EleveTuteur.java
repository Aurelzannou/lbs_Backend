package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_eleve_tuteur", schema = "lbs")
public class EleveTuteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_eltu_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_eltu_code", length = 50)
    private String code;

    @Column(name = "lbs_eltu_eleve_id", nullable = false)
    private Integer eleveId;

    @Column(name = "lbs_eltu_tuteur_id", nullable = false)
    private Integer tuteurId;

    @Column(name = "lbs_eltu_lien_parente", length = 50)
    private String lienParente; // Ex: PERE, MERE, ONCLE, TUTEUR

    @Column(name = "lbs_eltu_contact_urgence")
    private Boolean contactUrgence;

    @Column(name = "lbs_eltu_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_eltu_modifier_par", length = 100)
    private String modifierPar;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_eltu_eleve_id", insertable = false, updatable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_eltu_tuteur_id", insertable = false, updatable = false)
    private Tuteur tuteur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
        if (this.contactUrgence == null) this.contactUrgence = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifierLe = LocalDateTime.now();
    }

    // ===== GETTERS & SETTERS =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getEleveId() { return eleveId; }
    public void setEleveId(Integer eleveId) { this.eleveId = eleveId; }

    public Integer getTuteurId() { return tuteurId; }
    public void setTuteurId(Integer tuteurId) { this.tuteurId = tuteurId; }

    public String getLienParente() { return lienParente; }
    public void setLienParente(String lienParente) { this.lienParente = lienParente; }

    public Boolean getContactUrgence() { return contactUrgence; }
    public void setContactUrgence(Boolean contactUrgence) { this.contactUrgence = contactUrgence; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public Eleve getEleve() { return eleve; }
    public void setEleve(Eleve eleve) { this.eleve = eleve; }

    public Tuteur getTuteur() { return tuteur; }
    public void setTuteur(Tuteur tuteur) { this.tuteur = tuteur; }
}
