package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_type_frais", schema = "lbs")
@AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_tyfr_modifier_le"))
public class TypeFrais extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_tyfr_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_tyfr_code", length = 20)
    private String code;

    @Column(name = "lbs_tyfr_libelle", length = 100)
    private String libelle;

    @Column(name = "lbs_tyfr_obligatoire")
    private Boolean obligatoire;

    @Column(name = "lbs_tyfr_actif")
    private Boolean actif;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public TypeFrais() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Boolean getObligatoire() { return obligatoire; }
    public void setObligatoire(Boolean obligatoire) { this.obligatoire = obligatoire; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}