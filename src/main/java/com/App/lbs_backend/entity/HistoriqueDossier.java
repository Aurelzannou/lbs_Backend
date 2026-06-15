package com.App.lbs_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lbs_historique_dossier", schema = "lbs")
public class HistoriqueDossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_hist_dossier_id", nullable = false)
    private Long dossierId;

    @Column(name = "lbs_hist_action", length = 50, nullable = false)
    private String action;

    @Column(name = "lbs_hist_effectue_par", length = 100)
    private String effectuePar;

    @Column(name = "lbs_hist_effectue_le", nullable = false)
    private LocalDateTime effectueLe;

    @Column(name = "lbs_hist_commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @PrePersist
    public void prePersist() {
        if (this.effectueLe == null) this.effectueLe = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getDossierId() { return dossierId; }
    public void setDossierId(Long dossierId) { this.dossierId = dossierId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEffectuePar() { return effectuePar; }
    public void setEffectuePar(String effectuePar) { this.effectuePar = effectuePar; }
    public LocalDateTime getEffectueLe() { return effectueLe; }
    public void setEffectueLe(LocalDateTime effectueLe) { this.effectueLe = effectueLe; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
