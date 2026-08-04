package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_note", schema = "lbs", indexes = {
    @Index(name = "idx_note_eleve", columnList = "lbs_note_eleve_id"),
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

    @Column(name = "lbs_note_eleve_id", nullable = false)
    private Long eleveId;

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

    /** "INTERROGATION" ou "DEVOIR" */
    @Column(name = "lbs_note_type_evaluation", length = 50)
    private String typeEvaluation;

    /** Numéro d'ordre au sein du type d'évaluation : 1 ou 2 pour "DEVOIR" (colonnes "1er DEV"/
        "2e DEV" du bulletin) ; 1..N pour "INTERROGATION" (le professeur peut en saisir autant
        qu'il veut — leur moyenne alimente ensuite le calcul global). Sert de clé naturelle pour
        l'upsert avec (eleveId, matiereId, periodeId, typeEvaluation). */
    @Column(name = "lbs_note_numero")
    private Integer numero;

    @Column(name = "lbs_note_date_evaluation")
    private LocalDate dateEvaluation;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_eleve_id", insertable = false, updatable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_matiere_id", insertable = false, updatable = false)
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_periode_id", insertable = false, updatable = false)
    private PeriodeAcademique periode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_note_professeur_id", insertable = false, updatable = false)
    private Professeur professeur;

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

    public Long getEleveId() { return eleveId; }
    public void setEleveId(Long eleveId) { this.eleveId = eleveId; }

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

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public LocalDate getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDate dateEvaluation) { this.dateEvaluation = dateEvaluation; }

    public Eleve getEleve() { return eleve; }
    public void setEleve(Eleve eleve) { this.eleve = eleve; }

    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }

    public PeriodeAcademique getPeriode() { return periode; }
    public void setPeriode(PeriodeAcademique periode) { this.periode = periode; }

    public Professeur getProfesseur() { return professeur; }
    public void setProfesseur(Professeur professeur) { this.professeur = professeur; }
}
