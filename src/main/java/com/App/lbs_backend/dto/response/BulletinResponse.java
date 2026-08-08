package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class BulletinResponse {
    private Long eleveId;
    private String eleveNomComplet;
    private String eleveMatricule;
    private Long classeId;
    private String classeLibelle;
    private int effectifClasse;
    private Long periodeId;
    private String periodeLibelle;
    private String anneeScolaireLibelle;

    private List<BulletinMatiereResponse> matieres;

    private Double moyennePonderee;
    private Integer rangTrimestre;

    /** null tant qu'aucune autre période de l'année n'a de notes — pas de bulletin annuel encore
        significatif. */
    private Double moyenneAnnuelle;
    private Integer rangAnnuel;

    /** Moyenne pondérée de chaque période (trimestre) de l'année scolaire, dans l'ordre
        chronologique — alimente le récapitulatif "Moyenne du 1er/2e/3e Trimestre" du bulletin PDF. */
    private List<MoyennePeriodeResponse> moyennesParPeriode;

    // Mentions — choix persistés de l'administrateur (jamais écrasés une fois enregistrés)
    private Boolean tableauHonneur;
    private Boolean felicitations;
    private Boolean encouragement;
    private Boolean avertissement;
    private String decisionConseil;
    private String observationDirecteur;

    // Suggestions calculées par seuils — affichées à titre indicatif, jamais persistées
    private boolean suggestionFelicitations;
    private boolean suggestionTableauHonneur;
    private boolean suggestionAvertissement;
}
