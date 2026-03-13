package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_dossier_eleve", schema = "lbs")
public class DossierEleve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_doss_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_doss_code", length = 20)
    private String code;

    @Column(name = "lbs_doss_nom_eleve", length = 100)
    private String nomEleve;

    @Column(name = "lbs_doss_prenom_eleve", length = 100)
    private String prenomEleve;

    @Column(name = "lbs_doss_sexe", length = 10)
    private String sexe;

    @Column(name = "lbs_doss_age")
    private Integer age;

    @Column(name = "lbs_doss_matricule", length = 30)
    private String matricule;

    @Column(name = "lbs_doss_actif")
    private Boolean actif;

    @Column(name = "lbs_doss_classe_id")
    private Integer classeId;

    @Column(name = "lbs_doss_date_debut")
    private LocalDate dateDebut;

    @Column(name = "lbs_doss_date_fin")
    private LocalDate dateFin;

    @Column(name = "lbs_doss_soufant", length = 200)
    private String soufant;

    @Column(name = "lbs_doss_numero", length = 30)
    private String numero;

    @Column(name = "lbs_doss_utilisateur_id")
    private Integer utilisateurId;

    @Column(name = "lbs_doss_provenance", length = 200)
    private String provenance;

    @Column(name = "lbs_doss_type_operation_id")
    private Integer typeOperationId;

    @Column(name = "lbs_doss_acte_id")
    private Integer acteId;

    @Column(name = "lbs_doss_photo", length = 500)
    private String photo;

    @Column(name = "lbs_doss_annee_scolaire_id")
    private Integer anneeScolaireId;

    @Column(name = "lbs_doss_remise")
    private Double remise;

    @Column(name = "lbs_doss_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_doss_modifier_par", length = 100)
    private String modifierPar;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doss_classe_id", insertable = false, updatable = false)
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doss_type_operation_id", insertable = false, updatable = false)
    private TypeOperation typeOperation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doss_acte_id", insertable = false, updatable = false)
    private Acte acte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doss_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_doss_annee_scolaire_id", insertable = false, updatable = false)
    private AnneeScolaire anneeScolaire;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifierLe = LocalDateTime.now();
    }

    // ===== CONSTRUCTEURS =====
    public DossierEleve() {}

    // ===== GETTERS & SETTERS =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNomEleve() { return nomEleve; }
    public void setNomEleve(String nomEleve) { this.nomEleve = nomEleve; }

    public String getPrenomEleve() { return prenomEleve; }
    public void setPrenomEleve(String prenomEleve) { this.prenomEleve = prenomEleve; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public Integer getClasseId() { return classeId; }
    public void setClasseId(Integer classeId) { this.classeId = classeId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getSoufant() { return soufant; }
    public void setSoufant(String soufant) { this.soufant = soufant; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public Integer getTypeOperationId() { return typeOperationId; }
    public void setTypeOperationId(Integer typeOperationId) { this.typeOperationId = typeOperationId; }

    public Integer getActeId() { return acteId; }
    public void setActeId(Integer acteId) { this.acteId = acteId; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public Integer getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Integer anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public Double getRemise() { return remise; }
    public void setRemise(Double remise) { this.remise = remise; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }

    public TypeOperation getTypeOperation() { return typeOperation; }
    public void setTypeOperation(TypeOperation typeOperation) { this.typeOperation = typeOperation; }

    public Acte getActe() { return acte; }
    public void setActe(Acte acte) { this.acte = acte; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public AnneeScolaire getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) { this.anneeScolaire = anneeScolaire; }
}