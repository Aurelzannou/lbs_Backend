package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import com.App.lbs_backend.core.Timestamps;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lbs_periode_inscription", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe",  column = @Column(name = "lbs_pins_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_pins_modifier_par", length = 100))
})
public class PeriodeInscription extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_pins_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_pins_libelle", length = 150)
    private String libelle;

    @Column(name = "lbs_pins_annee_scolaire_id", nullable = false)
    private Long anneeScolaireId;

    @Column(name = "lbs_pins_date_ouverture", nullable = false)
    private LocalDate dateOuverture;

    @Column(name = "lbs_pins_date_cloture", nullable = false)
    private LocalDate dateCloture;

    @Column(name = "lbs_pins_actif")
    private Boolean actif = true;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public PeriodeInscription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Long getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Long anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public LocalDate getDateOuverture() { return dateOuverture; }
    public void setDateOuverture(LocalDate dateOuverture) { this.dateOuverture = dateOuverture; }

    public LocalDate getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDate dateCloture) { this.dateCloture = dateCloture; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}
