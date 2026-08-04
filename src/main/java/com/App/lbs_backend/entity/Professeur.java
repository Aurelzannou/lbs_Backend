package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_professeur", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_prfs_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_prfs_modifier_par", length = 100))
})
public class Professeur extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_prfs_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prfs_keycloak_id", length = 100, unique = true)
    private String keycloakId;

    @Column(name = "lbs_prfs_code", length = 20)
    private String code;

    @Column(name = "lbs_prfs_nom", length = 100)
    private String nom;

    @Column(name = "lbs_prfs_prenom", length = 100)
    private String prenom;

    @Column(name = "lbs_prfs_email", length = 150)
    private String email;

    @Column(name = "lbs_prfs_residence", length = 200)
    private String residence;

    @Column(name = "lbs_prfs_num", length = 30)
    private String num;

    @Column(name = "lbs_prfs_actif")
    private Boolean actif;

    /** Jeton à usage unique permettant au professeur de définir lui-même son mot de passe via un
        lien d'activation (jamais de mot de passe en clair envoyé par email). Effacé une fois
        utilisé ou remplacé à chaque nouvel envoi. */
    @Column(name = "lbs_prfs_activation_token", length = 100, unique = true)
    private String activationToken;

    @Column(name = "lbs_prfs_activation_expiration")
    private LocalDateTime activationTokenExpiration;

    /** Matières enseignées par ce professeur — utilisé pour filtrer les profs disponibles
        pour une matière donnée sur l'écran Emploi du temps. */
    @ElementCollection
    @CollectionTable(name = "lbs_professeur_matiere", schema = "lbs", joinColumns = @JoinColumn(name = "lbs_prfs_id"))
    @Column(name = "lbs_mati_id")
    private List<Long> matiereIds = new ArrayList<>();

    /** Classes affectées à ce professeur — source de vérité pour "quelles classes peut-il noter"
        (combiné à ses matières enseignées : il peut noter chacune de ses matières dans chacune de
        ses classes affectées), indépendamment de l'emploi du temps. */
    @ElementCollection
    @CollectionTable(name = "lbs_professeur_classe", schema = "lbs", joinColumns = @JoinColumn(name = "lbs_prfs_id"))
    @Column(name = "lbs_clas_id")
    private List<Long> classeIds = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Professeur() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getKeycloakId() { return keycloakId; }
    public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getResidence() { return residence; }
    public void setResidence(String residence) { this.residence = residence; }

    public String getNum() { return num; }
    public void setNum(String num) { this.num = num; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getActivationToken() { return activationToken; }
    public void setActivationToken(String activationToken) { this.activationToken = activationToken; }

    public LocalDateTime getActivationTokenExpiration() { return activationTokenExpiration; }
    public void setActivationTokenExpiration(LocalDateTime activationTokenExpiration) { this.activationTokenExpiration = activationTokenExpiration; }

    public List<Long> getMatiereIds() { return matiereIds; }
    public void setMatiereIds(List<Long> matiereIds) { this.matiereIds = matiereIds; }

    public List<Long> getClasseIds() { return classeIds; }
    public void setClasseIds(List<Long> classeIds) { this.classeIds = classeIds; }
}