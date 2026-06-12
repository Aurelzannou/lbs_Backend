package com.App.lbs_backend.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SoumettreInscriptionRequest {

    // Élève existant (optionnel)
    private Long eleveId;

    // Nouvel élève (si eleveId est null)
    private String nom;
    private String prenom;
    private String sexe;
    private LocalDate dateNaissance;

    // Dossier
    private Long classeId;
    private Long anneeScolaireId;

    // Tuteur (optionnel)
    private Long tuteurId;
}
