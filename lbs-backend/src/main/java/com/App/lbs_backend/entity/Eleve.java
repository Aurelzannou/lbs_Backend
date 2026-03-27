package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lbs_eleve", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_elev_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_elev_modifier_par", length = 100))
})
public class Eleve extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @Column(name = "lbs_elev_matricule", length = 30, unique = true)
    private String matricule;

    @Column(name = "lbs_elev_actif")
    private Boolean actif;

    @Column(name = "lbs_elev_souffrant", length = 200)
    private String souffrant;

    @Column(name = "lbs_elev_provenance", length = 200)
    private String provenance;

    @Column(name = "lbs_elev_photo", length = 500)
    private String photo;

    @Column(name = "lbs_elev_utilisateur_id")
    private Integer utilisateurId;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_elev_utilisateur_id", insertable = false, updatable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.actif == null) this.actif = true;
    }

    // ===== GETTERS & SETTERS =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

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

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getSouffrant() { return souffrant; }
    public void setSouffrant(String souffrant) { this.souffrant = souffrant; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
