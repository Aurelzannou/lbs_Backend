package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

/**
 * Journal de caisse (append-only) : chaque Paiement/DepenseScolaire enregistré ou annulé
 * génère une ligne ici, qui reste le seul historique fiable des mouvements de Caisse.solde.
 */
@Entity
@Table(name = "lbs_mouvement_caisse", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_movc_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_movc_modifier_par", length = 100))
})
public class MouvementCaisse extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_movc_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_movc_code", length = 20)
    private String code;

    @Column(name = "lbs_movc_caisse_id")
    private Long caisseId;

    @Column(name = "lbs_movc_type_mouvement", length = 10)
    private String typeMouvement;

    @Column(name = "lbs_movc_montant")
    private Double montant;

    @Column(name = "lbs_movc_date_mouvement")
    private LocalDateTime dateMouvement;

    @Column(name = "lbs_movc_source", length = 20)
    private String source;

    @Column(name = "lbs_movc_source_id")
    private Long sourceId;

    @Column(name = "lbs_movc_solde_apres")
    private Double soldeApres;

    @Column(name = "lbs_movc_description", length = 255)
    private String description;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_movc_caisse_id", insertable = false, updatable = false)
    private Caisse caisse;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public MouvementCaisse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getCaisseId() { return caisseId; }
    public void setCaisseId(Long caisseId) { this.caisseId = caisseId; }

    public String getTypeMouvement() { return typeMouvement; }
    public void setTypeMouvement(String typeMouvement) { this.typeMouvement = typeMouvement; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public Double getSoldeApres() { return soldeApres; }
    public void setSoldeApres(Double soldeApres) { this.soldeApres = soldeApres; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Caisse getCaisse() { return caisse; }
    public void setCaisse(Caisse caisse) { this.caisse = caisse; }
}
