package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FraisScolaireRequest implements FormRequest {
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    private String code;

    @NotNull(message = "L'année scolaire est obligatoire")
    private Long anneeScolaireId;

    @NotNull(message = "La classe est obligatoire")
    private Long classeId;

    @NotNull(message = "Le type de frais est obligatoire")
    private Long typeFraisId;

    @NotNull(message = "Le montant est obligatoire")
    private Double montant;

    private Boolean actif;
}
