package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_depense_scolaire", schema = "lbs")
public class DepenseScolaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_desc_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_desc_code", length = 20)
    private String code;

    @Column(name = "lbs_desc_reference", length = 50)
    private String reference;

    @Column(name = "lbs_desc_caisse_id")
    private Integer caisseId;

    @Column(name = "lbs_desc_categorie_depense_id")
    private Integer categorieDepenseId;

    @Column(name = "lbs_desc_montant")
    private Double montant;

    @Column(name = "lbs_desc_date_depense")
    private LocalDate dateDepense;

    @Column(name = "lbs_desc_motif", length = 500)
    private String motif;

    @Column(name = "lbs_desc_utilisateur_id")
    private Integer utilisateurId;

    @Column(name = "lbs_desc_modifier_le")
    private LocalDateTime modifierLe;

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
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifierLe = LocalDateTime.now(); }

    public DepenseScolaire() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getCaisseId() { return caisseId; }
    public void setCaisseId(Integer caisseId) { this.caisseId = caisseId; }

    public Integer getCategorieDepenseId() { return categorieDepenseId; }
    public void setCategorieDepenseId(Integer categorieDepenseId) { this.categorieDepenseId = categorieDepenseId; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public LocalDate getDateDepense() { return dateDepense; }
    public void setDateDepense(LocalDate dateDepense) { this.dateDepense = dateDepense; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public Caisse getCaisse() { return caisse; }
    public void setCaisse(Caisse caisse) { this.caisse = caisse; }

    public CategorieDepense getCategorieDepense() { return categorieDepense; }
    public void setCategorieDepense(CategorieDepense categorieDepense) { this.categorieDepense = categorieDepense; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}