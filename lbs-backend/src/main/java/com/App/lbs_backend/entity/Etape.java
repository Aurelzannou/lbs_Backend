package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lbs_etape", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_etap_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_etap_modifier_par", length = 100))
})
public class Etape extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_etap_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_etap_code", length = 20, unique = true)
    private String code;

    @Column(name = "lbs_etap_libelle", length = 100)
    private String libelle;

    @Column(name = "lbs_etap_ordre")
    private Integer ordre;

    @Column(name = "lbs_etap_actif")
    private Boolean actif;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.actif == null) this.actif = true;
    }

    public Etape() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}
