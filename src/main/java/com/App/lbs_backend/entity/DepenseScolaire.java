package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_depense_scolaire", schema = "lbs")
@AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_desc_modifier_le"))
public class DepenseScolaire extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_desc_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_desc_code", length = 20)
    private String code;

    @Column(name = "lbs_desc_reference", length = 50)
    private String reference;

    @Column(name = "lbs_desc_caisse_id")
    private Long caisseId;

    @Column(name = "lbs_desc_categorie_depense_id")
    private Long categorieDepenseId;

    @Column(name = "lbs_desc_montant")
    private Double montant;

    @Column(name = "lbs_desc_date_depense")
    private LocalDate dateDepense;

    @Column(name = "lbs_desc_motif", length = 500)
    private String motif;

    @Column(name = "lbs_desc_utilisateur_id")
    private Long utilisateurId;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_desc_caisse_id", insertable = false, updatable = false)
    private Caisse caisse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_desc_categorie_depense_id", insertable = false, updatable = false)
    private CategorieDepense categorieDepense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_desc_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public DepenseScolaire() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Long getCaisseId() { return caisseId; }
    public void setCaisseId(Long caisseId) { this.caisseId = caisseId; }

    public Long getCategorieDepenseId() { return categorieDepenseId; }
    public void setCategorieDepenseId(Long categorieDepenseId) { this.categorieDepenseId = categorieDepenseId; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public LocalDate getDateDepense() { return dateDepense; }
    public void setDateDepense(LocalDate dateDepense) { this.dateDepense = dateDepense; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public Caisse getCaisse() { return caisse; }
    public void setCaisse(Caisse caisse) { this.caisse = caisse; }

    public CategorieDepense getCategorieDepense() { return categorieDepense; }
    public void setCategorieDepense(CategorieDepense categorieDepense) { this.categorieDepense = categorieDepense; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}