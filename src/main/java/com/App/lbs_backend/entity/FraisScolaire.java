package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_frais_scolaire", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_frsc_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_frsc_modifier_par", length = 100))
})
public class FraisScolaire extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_frsc_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_frsc_code", length = 20)
    private String code;

    @Column(name = "lbs_frsc_annee_scolaire_id")
    private Long anneeScolaireId;

    @Column(name = "lbs_frsc_classe_id")
    private Long classeId;

    @Column(name = "lbs_frsc_type_frais_id")
    private Long typeFraisId;

    @Column(name = "lbs_frsc_montant")
    private Double montant;

    @Column(name = "lbs_frsc_actif")
    private Boolean actif;

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
    }

    public FraisScolaire() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Long anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public Long getClasseId() { return classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }

    public Long getTypeFraisId() { return typeFraisId; }
    public void setTypeFraisId(Long typeFraisId) { this.typeFraisId = typeFraisId; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public AnneeScolaire getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }

    public TypeFrais getTypeFrais() { return typeFrais; }
    public void setTypeFrais(TypeFrais typeFrais) { this.typeFrais = typeFrais; }
}