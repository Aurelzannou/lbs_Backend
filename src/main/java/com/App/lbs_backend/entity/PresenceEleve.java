package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_presence_eleve", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_prel_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_prel_modifier_par", length = 100))
})
public class PresenceEleve extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_prel_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prel_code", length = 30)
    private String code;

    @Column(name = "lbs_prel_emploi_temps_id")
    private Long emploiTempsId;

    @Column(name = "lbs_prel_date")
    private LocalDate date;

    @Column(name = "lbs_prel_eleve_id")
    private Long eleveId;

    @Column(name = "lbs_prel_statut", length = 20)
    private String statut;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_prel_emploi_temps_id", insertable = false, updatable = false)
    private EmploiDuTemps emploiDuTemps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_prel_eleve_id", insertable = false, updatable = false)
    private Eleve eleve;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public PresenceEleve() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getEmploiTempsId() { return emploiTempsId; }
    public void setEmploiTempsId(Long emploiTempsId) { this.emploiTempsId = emploiTempsId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Long getEleveId() { return eleveId; }
    public void setEleveId(Long eleveId) { this.eleveId = eleveId; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public EmploiDuTemps getEmploiDuTemps() { return emploiDuTemps; }
    public void setEmploiDuTemps(EmploiDuTemps emploiDuTemps) { this.emploiDuTemps = emploiDuTemps; }

    public Eleve getEleve() { return eleve; }
    public void setEleve(Eleve eleve) { this.eleve = eleve; }
}
