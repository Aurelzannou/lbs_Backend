package com.App.lbs_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DossierEtapeRequest {

    @NotNull(message = "Le dossier élève est obligatoire")
    private Integer dossierEleveId;

    @NotNull(message = "L'étape est obligatoire")
    private Integer etapeId;

    private String statut;
    private String commentaire;
    private Integer utilisateurId;
}
