package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_note", schema = "lbs")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_note_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_note_code", length = 20)
    private String code;

    @Column(name = "lbs_note_inscription_id", nullable = false)
    private Integer inscriptionId;

    @Column(name = "lbs_note_matiere_id", nullable = false)
    private Integer matiereId;

    @Column(name = "lbs_note_periode_id", nullable = false)
    private Integer periodeId;

    @Column(name = "lbs_note_professeur_id")
    private Integer professeurId;

    @Column(name = "lbs_note_valeur")
    private Double valeur; // Ex: 15.5

    @Column(name = "lbs_note_bareme")
    private Double bareme; // Ex: 20.0 pour une note sur 20

    @Column(name = "lbs_note_type_evaluation", length = 50)
    private String typeEvaluation; // Ex: DEVOIR, EXAMEN, INTERROGATION

    @Column(name = "lbs_note_date_evaluation")
    private LocalDate dateEvaluation;

    @Column(name = "lbs_note_commentaire", length = 500)
    private String commentaire; // Appréciation du professeur

    @Column(name = "lbs_note_utilisateur_id")
    private Integer utilisateurId; // Utilisateur (Secrétariat ou Prof) ayant saisi la note

    @Column(name = "lbs_note_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_note_modifier_par", length = 100)
    private String modifierPar;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_inscription_id", insertable = false, updatable = false)
    private Inscription inscription;

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
        this.modifierLe = LocalDateTime.now();
        if (this.bareme == null) this.bareme = 20.0;
        if (this.dateEvaluation == null) this.dateEvaluation = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifierLe = LocalDateTime.now();
    }

    public Note() {}

    // ===== GETTERS & SETTERS =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getInscriptionId() { return inscriptionId; }
    public void setInscriptionId(Integer inscriptionId) { this.inscriptionId = inscriptionId; }

    public Integer getMatiereId() { return matiereId; }
    public void setMatiereId(Integer matiereId) { this.matiereId = matiereId; }

    public Integer getPeriodeId() { return periodeId; }
    public void setPeriodeId(Integer periodeId) { this.periodeId = periodeId; }

    public Integer getProfesseurId() { return professeurId; }
    public void setProfesseurId(Integer professeurId) { this.professeurId = professeurId; }

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

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public Inscription getInscription() { return inscription; }
    public void setInscription(Inscription inscription) { this.inscription = inscription; }

    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }

    public PeriodeAcademique getPeriode() { return periode; }
    public void setPeriode(PeriodeAcademique periode) { this.periode = periode; }

    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
