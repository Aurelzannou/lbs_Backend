package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_inscription", schema = "lbs")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_insc_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_insc_code", length = 20)
    private String code;

    @Column(name = "lbs_insc_eleve_id")
    private Integer eleveId;

    @Column(name = "lbs_insc_classe_id")
    private Integer classeId;

    @Column(name = "lbs_insc_annee_scolaire_id")
    private Integer anneeScolaireId;

    @Column(name = "lbs_insc_date_debut")
    private LocalDate dateDebut;

    @Column(name = "lbs_insc_date_fin")
    private LocalDate dateFin;

    @Column(name = "lbs_insc_statut_id")
    private Integer statutId;

    @Column(name = "lbs_insc_remise")
    private Double remise;

    @Column(name = "lbs_insc_numero", length = 30)
    private String numero;

    @Column(name = "lbs_insc_type_operation_id")
    private Integer typeOperationId;

    @Column(name = "lbs_insc_acte_id")
    private Integer acteId;

    @Column(name = "lbs_insc_utilisateur_id")
    private Integer utilisateurId;

    @Column(name = "lbs_insc_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_insc_modifier_par", length = 100)
    private String modifierPar;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_insc_eleve_id", insertable = false, updatable = false)
    private Eleve eleve;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_insc_classe_id", insertable = false, updatable = false)
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_insc_annee_scolaire_id", insertable = false, updatable = false)
    private AnneeScolaire anneeScolaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_insc_type_operation_id", insertable = false, updatable = false)
    private TypeOperation typeOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_insc_acte_id", insertable = false, updatable = false)
    private Acte acte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_insc_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_insc_statut_id", insertable = false, updatable = false)
    private StatutInscription statut;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
        if (this.dateDebut == null) this.dateDebut = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifierLe = LocalDateTime.now();
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

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

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
}
