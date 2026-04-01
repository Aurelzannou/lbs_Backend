package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_coefficient", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_coef_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_coef_modifier_par", length = 100))
})
public class Coefficient extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_coef_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_coef_code", length = 20)
    private String code;

    @Column(name = "lbs_coef_matiere_id")
    private Long matiereId;

    @Column(name = "lbs_coef_niveau_id")
    private Long niveauId;

    @Column(name = "lbs_coef_valeur")
    private Double valeur;

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
        if (this.valeur == null) this.valeur = 1.0;
    }

    public Coefficient() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getMatiereId() { return matiereId; }
    public void setMatiereId(Long matiereId) { this.matiereId = matiereId; }

    public Long getNiveauId() { return niveauId; }
    public void setNiveauId(Long niveauId) { this.niveauId = niveauId; }

    public Double getValeur() { return valeur; }
    public void setValeur(Double valeur) { this.valeur = valeur; }

    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }

    public Niveau getNiveau() { return niveau; }
    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
}
