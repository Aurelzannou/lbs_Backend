package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lbs_profil", schema = "lbs")
public class Profil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_prof_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prof_code", length = 20)
    private String code;

    @Column(name = "lbs_prof_libelle", length = 100)
    private String libelle;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Profil() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}