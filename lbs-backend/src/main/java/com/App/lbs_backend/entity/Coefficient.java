package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_coefficient", schema = "lbs")
public class Coefficient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_coef_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_coef_code", length = 20)
    private String code;

    @Column(name = "lbs_coef_matiere_id")
    private Integer matiereId;

    @Column(name = "lbs_coef_niveau_id")
    private Integer niveauId;

    @Column(name = "lbs_coef_valeur")
    private Double valeur; // Ex: 4.0 (Coef 4)

    @Column(name = "lbs_coef_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_coef_modifier_par", length = 100)
    private String modifierPar;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_coef_matiere_id", insertable = false, updatable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_coef_niveau_id", insertable = false, updatable = false)
    private Niveau niveau;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
        if (this.valeur == null) this.valeur = 1.0;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifierLe = LocalDateTime.now();
    }

    public Coefficient() {}

    // ===== GETTERS & SETTERS =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getMatiereId() { return matiereId; }
    public void setMatiereId(Integer matiereId) { this.matiereId = matiereId; }

    public Integer getNiveauId() { return niveauId; }
    public void setNiveauId(Integer niveauId) { this.niveauId = niveauId; }

    public Double getValeur() { return valeur; }
    public void setValeur(Double valeur) { this.valeur = valeur; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }

    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
}
