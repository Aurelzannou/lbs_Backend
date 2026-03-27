package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lbs_dossier_eleve", schema = "lbs", indexes = {
    @Index(name = "idx_doel_eleve", columnList = "lbs_doel_eleve_id"),
    @Index(name = "idx_doel_classe", columnList = "lbs_doel_classe_id"),
    @Index(name = "idx_doel_annee", columnList = "lbs_doel_annee_scolaire_id"),
    @Index(name = "idx_doel_etape", columnList = "lbs_doel_etape_courante_id")
})
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_doel_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_doel_modifier_par", length = 100))
})
public class DossierEleve extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_doel_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_doel_code", length = 20)
    private String code;

    @Column(name = "lbs_doel_eleve_id")
    private Integer eleveId;

    @Column(name = "lbs_doel_classe_id")
    private Integer classeId;

    @Column(name = "lbs_doel_annee_scolaire_id")
    private Integer anneeScolaireId;

    @Column(name = "lbs_doel_date_debut")
    private LocalDate dateDebut;

    @Column(name = "lbs_doel_date_fin")
    private LocalDate dateFin;

    @Column(name = "lbs_doel_statut_id")
    private Integer statutId; // Type de statut (NOUVEAU, REDOUBLANT...)

    @Column(name = "lbs_doel_etape_courante_id")
    private Integer etapeCouranteId; // Phase du dossier (DÉPOSÉ, VALIDÉ, PAYÉ...)

    @Column(name = "lbs_doel_remise")
    private Double remise;

    @Column(name = "lbs_doel_numero", length = 30)
    private String numero;

    @Column(name = "lbs_doel_type_operation_id")
    private Integer typeOperationId;

    @Column(name = "lbs_doel_acte_id")
    private Integer acteId;

    @Column(name = "lbs_doel_utilisateur_id")
    private Integer utilisateurId;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_eleve_id", insertable = false, updatable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_classe_id", insertable = false, updatable = false)
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_annee_scolaire_id", insertable = false, updatable = false)
    private AnneeScolaire anneeScolaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_type_operation_id", insertable = false, updatable = false)
    private TypeOperation typeOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_acte_id", insertable = false, updatable = false)
    private Acte acte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_statut_id", insertable = false, updatable = false)
    private StatutInscription statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doel_etape_courante_id", insertable = false, updatable = false)
    private Etape etapeCourante;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.dateDebut == null) this.dateDebut = LocalDate.now();
    }

    // ===== GETTERS & SETTERS =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getEleveId() { return eleveId; }
    public void setEleveId(Integer eleveId) { this.eleveId = eleveId; }

    public Integer getClasseId() { return classeId; }
    public void setClasseId(Integer classeId) { this.classeId = classeId; }

    public Integer getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Integer anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public Integer getStatutId() { return statutId; }
    public void setStatutId(Integer statutId) { this.statutId = statutId; }

    public Integer getEtapeCouranteId() { return etapeCouranteId; }
    public void setEtapeCouranteId(Integer etapeCouranteId) { this.etapeCouranteId = etapeCouranteId; }

    public Double getRemise() { return remise; }
    public void setRemise(Double remise) { this.remise = remise; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Integer getTypeOperationId() { return typeOperationId; }
    public void setTypeOperationId(Integer typeOperationId) { this.typeOperationId = typeOperationId; }

    public Integer getActeId() { return acteId; }
    public void setActeId(Integer acteId) { this.acteId = acteId; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public Eleve getEleve() { return eleve; }
    public void setEleve(Eleve eleve) { this.eleve = eleve; }

    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }

    public AnneeScolaire getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) { this.anneeScolaire = anneeScolaire; }

    public TypeOperation getTypeOperation() { return typeOperation; }
    public void setTypeOperation(TypeOperation typeOperation) { this.typeOperation = typeOperation; }

    public Acte getActe() { return acte; }
    public void setActe(Acte acte) { this.acte = acte; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public StatutInscription getStatut() { return statut; }
    public void setStatut(StatutInscription statut) { this.statut = statut; }

    public Etape getEtapeCourante() { return etapeCourante; }
    public void setEtapeCourante(Etape etapeCourante) { this.etapeCourante = etapeCourante; }
}
