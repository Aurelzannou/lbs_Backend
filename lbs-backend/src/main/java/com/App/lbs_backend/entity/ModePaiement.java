package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lbs_mode_paiement", schema = "lbs")
@AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_mopa_modifier_le"))
public class ModePaiement extends AuditableEntity {

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

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

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
}