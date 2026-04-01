package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_note", schema = "lbs", indexes = {
    @Index(name = "idx_note_dossier", columnList = "lbs_note_dossier_eleve_id"),
    @Index(name = "idx_note_matiere", columnList = "lbs_note_matiere_id"),
    @Index(name = "idx_note_periode", columnList = "lbs_note_periode_id")
})
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_note_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_note_modifier_par", length = 100))
})
public class Note extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_note_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_note_code", length = 20)
    private String code;

    @Column(name = "lbs_note_dossier_eleve_id", nullable = false)
    private Long dossierEleveId;

    @Column(name = "lbs_note_matiere_id", nullable = false)
    private Long matiereId;

    @Column(name = "lbs_note_periode_id", nullable = false)
    private Long periodeId;

    @Column(name = "lbs_note_professeur_id")
    private Long professeurId;

    @Column(name = "lbs_note_valeur")
    private Double valeur;

    @Column(name = "lbs_note_bareme")
    private Double bareme;

    @Column(name = "lbs_note_type_evaluation", length = 50)
    private String typeEvaluation;

    @Column(name = "lbs_note_date_evaluation")
    private LocalDate dateEvaluation;

    @Column(name = "lbs_note_commentaire", length = 500)
    private String commentaire;

    @Column(name = "lbs_note_utilisateur_id")
    private Long utilisateurId;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_dossier_eleve_id", insertable = false, updatable = false)
    private DossierEleve dossierEleve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_matiere_id", insertable = false, updatable = false)
    private Matiere matiere;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_periode_id", insertable = false, updatable = false)
    private PeriodeAcademique periode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_professeur_id", insertable = false, updatable = false)
    private Professeur professeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.bareme == null) this.bareme = 20.0;
        if (this.dateEvaluation == null) this.dateEvaluation = LocalDate.now();
    }

    public Note() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getDossierEleveId() { return dossierEleveId; }
    public void setDossierEleveId(Long dossierEleveId) { this.dossierEleveId = dossierEleveId; }

    public Long getMatiereId() { return matiereId; }
    public void setMatiereId(Long matiereId) { this.matiereId = matiereId; }

    public Long getPeriodeId() { return periodeId; }
    public void setPeriodeId(Long periodeId) { this.periodeId = periodeId; }

    public Long getProfesseurId() { return professeurId; }
    public void setProfesseurId(Long professeurId) { this.professeurId = professeurId; }

    public Double getValeur() { return valeur; }
    public void setValeur(Double valeur) { this.valeur = valeur; }

    public Double getBareme() { return bareme; }
    public void setBareme(Double bareme) { this.bareme = bareme; }

    public String getTypeEvaluation() { return typeEvaluation; }
    public void setTypeEvaluation(String typeEvaluation) { this.typeEvaluation = typeEvaluation; }

    public LocalDate getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDate dateEvaluation) { this.dateEvaluation = dateEvaluation; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public DossierEleve getDossierEleve() { return dossierEleve; }
    public void setDossierEleve(DossierEleve dossierEleve) { this.dossierEleve = dossierEleve; }

    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }

    public PeriodeAcademique getPeriode() { return periode; }
    public void setPeriode(PeriodeAcademique periode) { this.periode = periode; }

    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
