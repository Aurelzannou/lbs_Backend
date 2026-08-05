package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

/** Progression du professeur dans la saisie des notes d'une classe/matière/période, colonne par
    colonne, PLUS le workflow de validation admin au niveau de la matière entière.

    Deux compteurs indépendants ("interrogationsVerroueesJusqua" et "devoirsVerrouesJusqua")
    représentent combien de colonnes sont verrouillées par le professeur, dans l'ordre — ex:
    interrogationsVerroueesJusqua=2 signifie que les interrogations 1 et 2 sont figées ; la colonne
    3 (si elle existe) devient alors la seule éditable, les suivantes restent inaccessibles tant que
    3 n'est pas verrouillée à son tour.

    L'"étape" (BROUILLON/SOUMISE/VALIDEE) est un workflow distinct, à la granularité de la matière
    entière (pas colonne par colonne) :
      BROUILLON : le professeur saisit encore, rien n'a été soumis.
      SOUMISE   : le professeur a explicitement soumis la matière pour validation (toutes ses
                  colonnes doivent être verrouillées avant de pouvoir soumettre) — il ne peut plus
                  rien modifier, l'admin peut encore éditer avant de valider.
      VALIDEE   : l'admin a validé — plus personne (même l'admin) ne peut modifier sans d'abord
                  "dévalider la matière" (qui repasse en SOUMISE, pas en BROUILLON : pas besoin de
                  refaire soumettre par le professeur pour une simple correction admin). */
@Entity
@Table(name = "lbs_progression_saisie", schema = "lbs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lbs_prgs_classe_id", "lbs_prgs_matiere_id", "lbs_prgs_periode_id"}))
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_prgs_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_prgs_modifier_par", length = 100))
})
public class ProgressionSaisieNote extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_prgs_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prgs_code", length = 20)
    private String code;

    @Column(name = "lbs_prgs_classe_id", nullable = false)
    private Long classeId;

    @Column(name = "lbs_prgs_matiere_id", nullable = false)
    private Long matiereId;

    @Column(name = "lbs_prgs_periode_id", nullable = false)
    private Long periodeId;

    @Column(name = "lbs_prgs_interro_verrouillees")
    private Integer interrogationsVerroueesJusqua;

    @Column(name = "lbs_prgs_devoirs_verrouilles")
    private Integer devoirsVerrouesJusqua;

    @Column(name = "lbs_prgs_etape_id")
    private Long etapeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_prgs_etape_id", insertable = false, updatable = false)
    private Etape etape;

    @Column(name = "lbs_prgs_date_soumission")
    private LocalDateTime dateSoumission;

    @Column(name = "lbs_prgs_date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "lbs_prgs_valide_par_email", length = 150)
    private String valideParEmail;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
        if (this.interrogationsVerroueesJusqua == null) this.interrogationsVerroueesJusqua = 0;
        if (this.devoirsVerrouesJusqua == null) this.devoirsVerrouesJusqua = 0;
    }

    public ProgressionSaisieNote() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getClasseId() { return classeId; }
    public void setClasseId(Long classeId) { this.classeId = classeId; }

    public Long getMatiereId() { return matiereId; }
    public void setMatiereId(Long matiereId) { this.matiereId = matiereId; }

    public Long getPeriodeId() { return periodeId; }
    public void setPeriodeId(Long periodeId) { this.periodeId = periodeId; }

    public Integer getInterrogationsVerroueesJusqua() { return interrogationsVerroueesJusqua; }
    public void setInterrogationsVerroueesJusqua(Integer v) { this.interrogationsVerroueesJusqua = v; }

    public Integer getDevoirsVerrouesJusqua() { return devoirsVerrouesJusqua; }
    public void setDevoirsVerrouesJusqua(Integer v) { this.devoirsVerrouesJusqua = v; }

    public Long getEtapeId() { return etapeId; }
    public void setEtapeId(Long etapeId) { this.etapeId = etapeId; }

    public Etape getEtape() { return etape; }
    public void setEtape(Etape etape) { this.etape = etape; }

    public LocalDateTime getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }

    public String getValideParEmail() { return valideParEmail; }
    public void setValideParEmail(String valideParEmail) { this.valideParEmail = valideParEmail; }
}
