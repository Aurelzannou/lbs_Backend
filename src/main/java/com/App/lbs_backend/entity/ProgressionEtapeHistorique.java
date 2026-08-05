package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

/** Historique des transitions d'étape (BROUILLON/SOUMISE/VALIDEE) d'une ProgressionSaisieNote —
    même esprit que DossierEtape pour DossierEleve : une ligne par transition, jamais modifiée ni
    supprimée, pour garder la trace complète même après plusieurs cycles de validation/dévalidation. */
@Entity
@Table(name = "lbs_progression_etape_historique", schema = "lbs", indexes = {
    @Index(name = "idx_pgeh_progression", columnList = "lbs_pgeh_progression_id")
})
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_pgeh_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_pgeh_modifier_par", length = 100))
})
public class ProgressionEtapeHistorique extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_pgeh_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_pgeh_code", length = 20)
    private String code;

    @Column(name = "lbs_pgeh_progression_id", nullable = false)
    private Long progressionId;

    @Column(name = "lbs_pgeh_etape_id", nullable = false)
    private Long etapeId;

    @Column(name = "lbs_pgeh_date_transition", nullable = false)
    private LocalDateTime dateTransition;

    @Column(name = "lbs_pgeh_auteur_email", length = 150)
    private String auteurEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_pgeh_progression_id", insertable = false, updatable = false)
    private ProgressionSaisieNote progression;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_pgeh_etape_id", insertable = false, updatable = false)
    private Etape etape;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.code == null) this.code = "HIST-" + this.uuid.substring(0, 8).toUpperCase();
        if (this.dateTransition == null) this.dateTransition = LocalDateTime.now();
    }

    public ProgressionEtapeHistorique() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getProgressionId() { return progressionId; }
    public void setProgressionId(Long progressionId) { this.progressionId = progressionId; }

    public Long getEtapeId() { return etapeId; }
    public void setEtapeId(Long etapeId) { this.etapeId = etapeId; }

    public LocalDateTime getDateTransition() { return dateTransition; }
    public void setDateTransition(LocalDateTime dateTransition) { this.dateTransition = dateTransition; }

    public String getAuteurEmail() { return auteurEmail; }
    public void setAuteurEmail(String auteurEmail) { this.auteurEmail = auteurEmail; }

    public ProgressionSaisieNote getProgression() { return progression; }
    public void setProgression(ProgressionSaisieNote progression) { this.progression = progression; }

    public Etape getEtape() { return etape; }
    public void setEtape(Etape etape) { this.etape = etape; }
}
