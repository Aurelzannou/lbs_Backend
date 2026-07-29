package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

/** Mentions (Tableau d'Honneur/Félicitations/Encouragement/Avertissement) et texte libre
    (décision du conseil, observation du directeur) d'un élève pour une période — des choix
    humains de l'administrateur, jamais recalculés automatiquement une fois enregistrés (seule une
    suggestion initiale par seuils est proposée à la création, voir BulletinService). */
@Entity
@Table(name = "lbs_bulletin_mention", schema = "lbs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lbs_bume_eleve_id", "lbs_bume_periode_id"}))
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_bume_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_bume_modifier_par", length = 100))
})
public class BulletinMention extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_bume_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_bume_code", length = 20)
    private String code;

    @Column(name = "lbs_bume_eleve_id", nullable = false)
    private Long eleveId;

    @Column(name = "lbs_bume_periode_id", nullable = false)
    private Long periodeId;

    @Column(name = "lbs_bume_tableau_honneur")
    private Boolean tableauHonneur;

    @Column(name = "lbs_bume_felicitations")
    private Boolean felicitations;

    @Column(name = "lbs_bume_encouragement")
    private Boolean encouragement;

    @Column(name = "lbs_bume_avertissement")
    private Boolean avertissement;

    @Column(name = "lbs_bume_decision_conseil", length = 500)
    private String decisionConseil;

    @Column(name = "lbs_bume_observation_directeur", length = 500)
    private String observationDirecteur;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.tableauHonneur == null) this.tableauHonneur = false;
        if (this.felicitations == null) this.felicitations = false;
        if (this.encouragement == null) this.encouragement = false;
        if (this.avertissement == null) this.avertissement = false;
    }

    public BulletinMention() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getEleveId() { return eleveId; }
    public void setEleveId(Long eleveId) { this.eleveId = eleveId; }

    public Long getPeriodeId() { return periodeId; }
    public void setPeriodeId(Long periodeId) { this.periodeId = periodeId; }

    public Boolean getTableauHonneur() { return tableauHonneur; }
    public void setTableauHonneur(Boolean tableauHonneur) { this.tableauHonneur = tableauHonneur; }

    public Boolean getFelicitations() { return felicitations; }
    public void setFelicitations(Boolean felicitations) { this.felicitations = felicitations; }

    public Boolean getEncouragement() { return encouragement; }
    public void setEncouragement(Boolean encouragement) { this.encouragement = encouragement; }

    public Boolean getAvertissement() { return avertissement; }
    public void setAvertissement(Boolean avertissement) { this.avertissement = avertissement; }

    public String getDecisionConseil() { return decisionConseil; }
    public void setDecisionConseil(String decisionConseil) { this.decisionConseil = decisionConseil; }

    public String getObservationDirecteur() { return observationDirecteur; }
    public void setObservationDirecteur(String observationDirecteur) { this.observationDirecteur = observationDirecteur; }
}
