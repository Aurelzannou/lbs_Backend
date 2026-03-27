package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lbs_echeancier", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_eche_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_eche_modifier_par", length = 100))
})
public class Echeancier extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_eche_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_eche_code", length = 20)
    private String code;

    @Column(name = "lbs_eche_frais_scolaire_id")
    private Integer fraisScolaireId;

    @Column(name = "lbs_eche_numero")
    private Integer numero;

    @Column(name = "lbs_eche_libelle", length = 100)
    private String libelle;

    @Column(name = "lbs_eche_date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "lbs_eche_montant")
    private Double montant;

    // ===== RELATION =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_eche_frais_scolaire_id", insertable = false, updatable = false)
    private FraisScolaire fraisScolaire;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Echeancier() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getFraisScolaireId() { return fraisScolaireId; }
    public void setFraisScolaireId(Integer fraisScolaireId) { this.fraisScolaireId = fraisScolaireId; }

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public FraisScolaire getFraisScolaire() { return fraisScolaire; }
    public void setFraisScolaire(FraisScolaire fraisScolaire) { this.fraisScolaire = fraisScolaire; }
}