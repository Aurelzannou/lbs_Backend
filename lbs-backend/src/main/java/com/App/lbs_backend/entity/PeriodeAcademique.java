package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lbs_periode_academique", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_peac_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_peac_modifier_par", length = 100))
})
public class PeriodeAcademique extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_peac_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_peac_code", length = 20)
    private String code;

    @Column(name = "lbs_peac_libelle", length = 150)
    private String libelle;

    @Column(name = "lbs_peac_annee_scolaire_id")
    private Integer anneeScolaireId;

    @Column(name = "lbs_peac_date_debut")
    private LocalDate dateDebut;

    @Column(name = "lbs_peac_date_fin")
    private LocalDate dateFin;

    @Column(name = "lbs_peac_verrouille")
    private Boolean verrouille;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_peac_annee_scolaire_id", insertable = false, updatable = false)
    private AnneeScolaire anneeScolaire;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.verrouille == null) this.verrouille = false;
    }

    public PeriodeAcademique() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Integer getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Integer anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public Boolean getVerrouille() { return verrouille; }
    public void setVerrouille(Boolean verrouille) { this.verrouille = verrouille; }

    public AnneeScolaire getAnneeScolaire() { return anneeScolaire; }
    public void setAnneeScolaire(AnneeScolaire anneeScolaire) { this.anneeScolaire = anneeScolaire; }
}
