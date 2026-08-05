package com.App.lbs_backend.dto.response;

import lombok.Data;

@Data
public class BulletinMatiereResponse {
    private Long matiereId;
    private String matiereLibelle;
    /** null si aucun coefficient n'est configuré pour cette matière au niveau de l'élève — la
        matière reste affichée si des notes existent, mais est exclue du calcul pondéré. */
    private Double coefficient;
    private Double interrogation;
    private Double devoir1;
    private Double devoir2;
    private Double moyenne;
    private Double moyenneCoefficientee;
    private Integer rang;
    private String appreciation;
    /** true si l'élève n'a strictement aucune note dans cette matière pour la période — la moyenne
        compte quand même 0 dans les calculs, mais l'appréciation affiche "N'a pas composé" plutôt
        qu'un jugement de niveau ("Insuffisant") qui serait trompeur pour une matière jamais évaluée. */
    private boolean nonCompose;
}
