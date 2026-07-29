package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class FeuillePresenceResponse {
    private Long emploiTempsId;
    private LocalDate date;
    private String jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private String classeLibelle;
    private String matiereLibelle;
    private Long profId;
    private String profNomComplet;
    private String profStatut;
    private List<EleveStatutDto> eleves;

    @Data
    public static class EleveStatutDto {
        private Long eleveId;
        private String nom;
        private String prenom;
        private String statut;
    }
}
