package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

/** Verrou de validation des notes/bulletins d'une classe pour une période donnée. Tant que
    `valide=false`, les professeurs/admin peuvent saisir/modifier les notes de cette classe pour
    cette période ; une fois validée, plus aucune écriture n'est acceptée (voir NoteService). */
@Entity
@Table(name = "lbs_validation_bulletin", schema = "lbs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lbs_vabu_classe_id", "lbs_vabu_periode_id"}))
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_vabu_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_vabu_modifier_par", length = 100))
})
public class ValidationBulletin extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_vabu_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_vabu_code", length = 20)
    private String code;

    @Column(name = "lbs_vabu_classe_id", nullable = false)
    private Long classeId;

    @Column(name = "lbs_vabu_periode_id", nullable = false)
    private Long periodeId;

    @Column(name = "lbs_vabu_annee_scolaire_id", nullable = false)
    private Long anneeScolaireId;

    @Column(name = "lbs_vabu_valide")
    private Boolean valide;

    @Column(name = "lbs_vabu_date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "lbs_vabu_valide_par_email", length = 150)
    private String valideParEmail;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.valide == null) this.valide = false;
    }

    public ValidationBulletin() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getClasseId() { return classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }

    public Long getPeriodeId() { return periodeId; }
    public void setPeriodeId(Long periodeId) { this.periodeId = periodeId; }

    public Long getAnneeScolaireId() { return anneeScolaireId; }
    public void setAnneeScolaireId(Long anneeScolaireId) { this.anneeScolaireId = anneeScolaireId; }

    public Boolean getValide() { return valide; }
    public void setValide(Boolean valide) { this.valide = valide; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }

    public String getValideParEmail() { return valideParEmail; }
    public void setValideParEmail(String valideParEmail) { this.valideParEmail = valideParEmail; }
}
