package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_mode_paiement", schema = "lbs")
public class ModePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_mopa_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_mopa_code", length = 20)
    private String code;

    @Column(name = "lbs_mopa_libelle", length = 100)
    private String libelle;

    @Column(name = "lbs_mopa_actif")
    private Boolean actif;

    @Column(name = "lbs_mopa_modifier_le")
    private LocalDateTime modifierLe;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifierLe = LocalDateTime.now(); }

    public ModePaiement() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }
}