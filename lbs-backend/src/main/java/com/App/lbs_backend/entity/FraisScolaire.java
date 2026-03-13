package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_frais_scolaire", schema = "lbs")
public class FraisScolaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_frsc_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_frsc_code", length = 20)
    private String code;

    @Column(name = "lbs_frsc_annee_scolaire_id")
    private Integer anneeScolaireId;

    @Column(name = "lbs_frsc_classe_id")
    private Integer classeId;

    @Column(name = "lbs_frsc_type_frais_id")
    private Integer typeFraisId;

    @Column(name = "lbs_frsc_montant")
    private Double montant;

    @Column(name = "lbs_frsc_actif")
    private Boolean actif;

    @Column(name = "lbs_frsc_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_frsc_modifier_par", length = 100)
    private String modifierPar;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_frsc_annee_scolaire_id", insertable = false, updatable = false)
    private AnneeScolaire anneeScolaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_frsc_classe_id", insertable = false, updatable = false)
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_frsc_type_frais_id", insertable = false, updatable = false)
    private TypeFrais typeFrais;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifierLe = LocalDateTime.now(); }

    public FraisScolaire() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Integer anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public Integer getClasseId() { return classeId; }
    public void setClasseId(Integer classeId) { this.classeId = classeId; }

    public Integer getTypeFraisId() { return typeFraisId; }
    public void setTypeFraisId(Integer typeFraisId) { this.typeFraisId = typeFraisId; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public AnneeScolaire getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }

    public TypeFrais getTypeFrais() { return typeFrais; }
    public void setTypeFrais(TypeFrais typeFrais) { this.typeFrais = typeFrais; }
}