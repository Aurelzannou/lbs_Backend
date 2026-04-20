package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CoefficientRequest implements FormRequest {
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    private String code;

    @NotNull(message = "La matière est obligatoire")
    private Long matiereId;

    @NotNull(message = "Le niveau est obligatoire")
    private Long niveauId;

    @NotNull(message = "La valeur du coefficient est obligatoire")
    private Double valeur;
}
