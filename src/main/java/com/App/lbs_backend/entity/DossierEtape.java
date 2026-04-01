package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_dossier_etape", schema = "lbs", indexes = {
    @Index(name = "idx_doet_dossier", columnList = "lbs_doet_dossier_eleve_id"),
    @Index(name = "idx_doet_etape", columnList = "lbs_doet_etape_id")
})
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_doet_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_doet_modifier_par", length = 100))
})
public class DossierEtape extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_doet_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_doet_dossier_eleve_id", nullable = false)
    private Long dossierEleveId;

    @Column(name = "lbs_doet_etape_id", nullable = false)
    private Long etapeId;

    @Column(name = "lbs_doet_statut", length = 50)
    private String statut; // EN_COURS, VALIDE, REJETE

    @Column(name = "lbs_doet_date_traitement")
    private LocalDateTime dateTraitement;

    @Column(name = "lbs_doet_commentaire", length = 500)
    private String commentaire;

    @Column(name = "lbs_doet_utilisateur_id")
    private Long utilisateurId;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_dossier_eleve_id", insertable = false, updatable = false)
    private DossierEleve dossierEleve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doet_etape_id", insertable = false, updatable = false)
    private Etape etape;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doet_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.dateTraitement == null) this.dateTraitement = LocalDateTime.now();
        if (this.statut == null) this.statut = "EN_COURS";
    }

    public DossierEtape() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Long getDossierEleveId() { return dossierEleveId; }
    public void setDossierEleveId(Long dossierEleveId) { this.dossierEleveId = dossierEleveId; }

    public Long getEtapeId() { return etapeId; }
    public void setEtapeId(Long etapeId) { this.etapeId = etapeId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public DossierEleve getDossierEleve() { return dossierEleve; }
    public void setDossierEleve(DossierEleve dossierEleve) { this.dossierEleve = dossierEleve; }

    public Etape getEtape() { return etape; }
    public void setEtape(Etape etape) { this.etape = etape; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
