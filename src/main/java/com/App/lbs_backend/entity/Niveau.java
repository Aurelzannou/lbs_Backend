package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import com.App.lbs_backend.core.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_niveau", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_nive_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_nive_modifier_par", length = 100))
})
public class Niveau extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_nive_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_nive_code", length = 20)
    private String code;

    @Column(name = "lbs_nive_libelle", length = 100)
    private String libelle;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Niveau() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
