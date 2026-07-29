package com.App.lbs_backend.dto.response;

import lombok.Data;
import java.time.LocalTime;

@Data
public class EmploiDuTempsResponse {
    private Long id;
    private String uuid;
    private String code;
    private Long classeId;
    private String classeLibelle;
    private Long anneeScolaireId;
    private String anneeScolaireLibelle;
    private Long matiereId;
    private String matiereLibelle;
    private Long profId;
    private String profNomComplet;
    private Boolean profActif;
    private String jour;
    private LocalTime heureDebut;
    private LocalTime heureFin;
}
