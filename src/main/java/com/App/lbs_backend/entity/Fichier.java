package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_fichier", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_fich_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_fich_modifier_par", length = 100))
})
public class Fichier extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_fich_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_fich_repertoir", length = 500)
    private String repertoir;

    @Column(name = "lbs_fich_code", length = 20)
    private String code;

    @Column(name = "lbs_fich_nom", length = 200)
    private String nom;

    @Column(name = "lbs_fich_taille")
    private Long taille;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Fichier() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getRepertoir() { return repertoir; }
    public void setRepertoir(String repertoir) { this.repertoir = repertoir; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Long getTaille() { return taille; }
    public void setTaille(Long taille) { this.taille = taille; }
}