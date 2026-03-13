package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lbs_acte", schema = "lbs")
public class Acte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_acte_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_acte_code", length = 20)
    private String code;

    @Column(name = "lbs_acte_reference", length = 50)
    private String reference;

    @Column(name = "lbs_acte_type_acte_id")
    private Integer typeActeId;

    @Column(name = "lbs_acte_chemin_fichier", length = 500)
    private String cheminFichier;

    @Column(name = "lbs_acte_nom_fichier", length = 200)
    private String nomFichier;

    @Column(name = "lbs_acte_fichier_id")
    private Integer fichierId;

    @Column(name = "lbs_acte_modifier_le")
    private LocalDateTime modifierLe;

    @Column(name = "lbs_acte_modifier_par", length = 100)
    private String modifierPar;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_acte_type_acte_id", insertable = false, updatable = false)
    private TypeActe typeActe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_acte_fichier_id", insertable = false, updatable = false)
    private Fichier fichier;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        this.modifierLe = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() { this.modifierLe = LocalDateTime.now(); }

    public Acte() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getTypeActeId() { return typeActeId; }
    public void setTypeActeId(Integer typeActeId) { this.typeActeId = typeActeId; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public Integer getFichierId() { return fichierId; }
    public void setFichierId(Integer fichierId) { this.fichierId = fichierId; }

    public LocalDateTime getModifierLe() { return modifierLe; }
    public void setModifierLe(LocalDateTime modifierLe) { this.modifierLe = modifierLe; }

    public String getModifierPar() { return modifierPar; }
    public void setModifierPar(String modifierPar) { this.modifierPar = modifierPar; }

    public TypeActe getTypeActe() { return typeActe; }
    public void setTypeActe(TypeActe typeActe) { this.typeActe = typeActe; }

    public Fichier getFichier() { return fichier; }
    public void setFichier(Fichier fichier) { this.fichier = fichier; }
}