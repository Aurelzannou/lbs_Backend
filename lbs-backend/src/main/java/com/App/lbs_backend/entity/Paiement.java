package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_paiement", schema = "lbs")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_paie_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_paie_code", length = 20)
    private String code;

    @Column(name = "lbs_paie_reference", length = 50)
    private String reference;

    @Column(name = "lbs_paie_inscription_id")
    private Integer inscriptionId;

    @Column(name = "lbs_paie_frais_scolaire_id")
    private Integer fraisScolaireId;

    @Column(name = "lbs_paie_date_paiement")
    private LocalDate datePaiement;

    @Column(name = "lbs_paie_montant")
    private Double montant;

    @Column(name = "lbs_paie_mode_paiement_id")
    private Integer modePaiementId;

    @Column(name = "lbs_paie_caisse_id")
    private Integer caisseId;

    @Column(name = "lbs_paie_utilisateur_id")
    private Integer utilisateurId;

    @Column(name = "lbs_paie_observation", length = 500)
    private String observation;

    @Column(name = "lbs_paie_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_paie_modifier_par", length = 100)
    private String modifierPar;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_paie_inscription_id", insertable = false, updatable = false)
    private Inscription inscription;

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
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifierLe = LocalDateTime.now(); }

    public Paiement() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(Integer inscriptionId) { this.inscriptionId = inscriptionId; }

    public Integer getFraisScolaireId() { return fraisScolaireId; }
    public void setFraisScolaireId(Integer fraisScolaireId) { this.fraisScolaireId = fraisScolaireId; }

    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public Integer getModePaiementId() { return modePaiementId; }
    public void setModePaiementId(Integer modePaiementId) { this.modePaiementId = modePaiementId; }

    public Integer getCaisseId() { return caisseId; }
    public void setCaisseId(Integer caisseId) { this.caisseId = caisseId; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public Inscription getInscription() { return inscription; }
    public void setInscription(Inscription inscription) { this.inscription = inscription; }

    public FraisScolaire getFraisScolaire() { return fraisScolaire; }
    public void setFraisScolaire(FraisScolaire fraisScolaire) { this.fraisScolaire = fraisScolaire; }

    public ModePaiement getModePaiement() { return modePaiement; }
    public void setModePaiement(ModePaiement modePaiement) { this.modePaiement = modePaiement; }

    public Caisse getCaisse() { return caisse; }
    public void setCaisse(Caisse caisse) { this.caisse = caisse; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}