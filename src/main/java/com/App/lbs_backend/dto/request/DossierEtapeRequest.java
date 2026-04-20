package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DossierEtapeRequest implements FormRequest {

    @NotNull(message = "Le dossier élève est obligatoire")
    private Long dossierEleveId;

    @NotNull(message = "L'étape est obligatoire")
    private Long etapeId;

    private String statut;
    private String commentaire;
    private Long utilisateurId;
}
