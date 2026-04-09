package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_acte", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_acte_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_acte_modifier_par", length = 100))
})
public class Acte extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_acte_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_acte_code", length = 20)
    private String code;

    @Column(name = "lbs_acte_reference", length = 50)
    private String reference;

    @Column(name = "lbs_acte_type_acte_id")
    private Long typeActeId;

    @Column(name = "lbs_acte_chemin_fichier", length = 500)
    private String cheminFichier;

    @Column(name = "lbs_acte_nom_fichier", length = 200)
    private String nomFichier;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_acte_type_acte_id", insertable = false, updatable = false)
    private TypeActe typeActe;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Acte() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Long getTypeActeId() { return typeActeId; }
    public void setTypeActeId(Long typeActeId) { this.typeActeId = typeActeId; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public TypeActe getTypeActe() { return typeActe; }
    public void setTypeActe(TypeActe typeActe) { this.typeActe = typeActe; }
}