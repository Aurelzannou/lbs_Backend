package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_eleve", schema = "lbs", indexes = {
    @Index(name = "idx_elev_nom", columnList = "lbs_elev_nom"),
    @Index(name = "idx_elev_prenom", columnList = "lbs_elev_prenom")
})
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_elev_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_elev_modifier_par", length = 100))
})
public class Eleve extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_elev_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_elev_code", length = 20)
    private String code;

    @Column(name = "lbs_elev_nom", length = 100)
    private String nom;

    @Column(name = "lbs_elev_prenom", length = 100)
    private String prenom;

    @Column(name = "lbs_elev_sexe", length = 10)
    private String sexe;

    @Column(name = "lbs_elev_date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lbs_elev_actif")
    private Boolean actif;

    @Column(name = "lbs_elev_souffrant")
    private Boolean souffrant;

    @Column(name = "lbs_elev_provenance", length = 200)
    private String provenance;

    @Column(name = "lbs_elev_photo", length = 500)
    private String photo;

    @Column(name = "lbs_elev_utilisateur_id")
    private Long utilisateurId;

    @Column(name = "lbs_elev_classe_id")
    private Long classeId;

    @Column(name = "lbs_elev_tuteur_id")
    private Long tuteurId;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_elev_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_elev_classe_id", insertable = false, updatable = false)
    private Classe classe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_elev_tuteur_id", insertable = false, updatable = false)
    private Tuteur tuteur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.actif == null) this.actif = true;
    }

    // ===== GETTERS & SETTERS =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }



    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public Boolean getSouffrant() { return souffrant; }
    public void setSouffrant(Boolean souffrant) { this.souffrant = souffrant; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }

    public Long getClasseId() { return classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }

    public Long getTuteurId() { return tuteurId; }
    public void setTuteurId(Long tuteurId) { this.tuteurId = tuteurId; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public Classe getClasse() { return classe; }
    public void setClasse(Classe classe) { this.classe = classe; }

    public Tuteur getTuteur() { return tuteur; }
    public void setTuteur(Tuteur tuteur) { this.tuteur = tuteur; }
}
