package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_tuteur", schema = "lbs")
public class Tuteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_tute_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_tute_code", length = 20)
    private String code;

    @Column(name = "lbs_tute_nom", length = 100)
    private String nom;

    @Column(name = "lbs_tute_prenom", length = 100)
    private String prenom;

    @Column(name = "lbs_tute_telephone1", length = 20)
    private String telephone1;

    @Column(name = "lbs_tute_telephone2", length = 20)
    private String telephone2;

    @Column(name = "lbs_tute_email", length = 100)
    private String email;

    @Column(name = "lbs_tute_profession", length = 100)
    private String profession;

    @Column(name = "lbs_tute_adresse", length = 250)
    private String adresse;

    @Column(name = "lbs_tute_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_tute_modifier_par", length = 100)
    private String modifierPar;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
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

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone1() { return telephone1; }
    public void setTelephone1(String telephone1) { this.telephone1 = telephone1; }

    public String getTelephone2() { return telephone2; }
    public void setTelephone2(String telephone2) { this.telephone2 = telephone2; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }
}
