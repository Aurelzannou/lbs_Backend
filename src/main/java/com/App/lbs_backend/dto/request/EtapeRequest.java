package com.App.lbs_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EtapeRequest {
    
    @NotBlank(message = "Le code de l'étape est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    private String code;

    @NotBlank(message = "Le libellé de l'étape est obligatoire")
    @Size(max = 100, message = "Le libellé ne peut pas dépasser 100 caractères")
    private String libelle;

    @NotNull(message = "L'ordre de l'étape est obligatoire")
    private Integer ordre;

    private Boolean actif;
}
