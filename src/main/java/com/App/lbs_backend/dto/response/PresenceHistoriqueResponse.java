package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PresenceHistoriqueResponse {
    private LocalDate date;
    private String jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String matiereLibelle;
    private String statut;
}
