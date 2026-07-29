package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.time.LocalTime;

/**
 * Une séance de l'emploi du temps du jour choisi, avec l'indication si la présence a
 * déjà été prise pour cette date précise.
 */
@Data
public class SeanceJourResponse {
    private Long emploiTempsId;
    private String jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String matiereLibelle;
    private String profNomComplet;
    private boolean presenceEnregistree;
}
