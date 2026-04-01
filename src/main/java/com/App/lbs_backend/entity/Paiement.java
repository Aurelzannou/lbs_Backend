package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_paiement", schema = "lbs", indexes = {
    @Index(name = "idx_paie_dossier", columnList = "lbs_paie_dossier_eleve_id"),
    @Index(name = "idx_paie_date", columnList = "lbs_paie_date_paiement")
})
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_paie_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_paie_modifier_par", length = 100))
})
public class Paiement extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_paie_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_paie_code", length = 20)
    private String code;

    @Column(name = "lbs_paie_reference", length = 50)
    private String reference;

    @Column(name = "lbs_paie_dossier_eleve_id")
    private Long dossierEleveId;

    @Column(name = "lbs_paie_frais_scolaire_id")
    private Long fraisScolaireId;

    @Column(name = "lbs_paie_date_paiement")
    private LocalDate datePaiement;

    @Column(name = "lbs_paie_montant")
    private Double montant;

    @Column(name = "lbs_paie_mode_paiement_id")
    private Long modePaiementId;

    @Column(name = "lbs_paie_caisse_id")
    private Long caisseId;

    @Column(name = "lbs_paie_utilisateur_id")
    private Long utilisateurId;

    @Column(name = "lbs_paie_observation", length = 500)
    private String observation;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_paie_dossier_eleve_id", insertable = false, updatable = false)
    private DossierEleve dossierEleve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_paie_frais_scolaire_id", insertable = false, updatable = false)
    private FraisScolaire fraisScolaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_paie_mode_paiement_id", insertable = false, updatable = false)
    private ModePaiement modePaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_paie_caisse_id", insertable = false, updatable = false)
    private Caisse caisse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_paie_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Paiement() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Long getDossierEleveId() { return dossierEleveId; }
    public void setDossierEleveId(Long dossierEleveId) { this.dossierEleveId = dossierEleveId; }

    public Long getFraisScolaireId() { return fraisScolaireId; }
    public void setFraisScolaireId(Long fraisScolaireId) { this.fraisScolaireId = fraisScolaireId; }

    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public Long getModePaiementId() { return modePaiementId; }
    public void setModePaiementId(Long modePaiementId) { this.modePaiementId = modePaiementId; }

    public Long getCaisseId() { return caisseId; }
    public void setCaisseId(Long caisseId) { this.caisseId = caisseId; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public DossierEleve getDossierEleve() { return dossierEleve; }
    public void setDossierEleve(DossierEleve dossierEleve) { this.dossierEleve = dossierEleve; }

    public FraisScolaire getFraisScolaire() { return fraisScolaire; }
    public void setFraisScolaire(FraisScolaire fraisScolaire) { this.fraisScolaire = fraisScolaire; }

    public ModePaiement getModePaiement() { return modePaiement; }
    public void setModePaiement(ModePaiement modePaiement) { this.modePaiement = modePaiement; }

    public Caisse getCaisse() { return caisse; }
    public void setCaisse(Caisse caisse) { this.caisse = caisse; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}