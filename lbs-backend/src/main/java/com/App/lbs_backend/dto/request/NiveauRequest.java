package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;

public record NiveauRequest(
        @NotBlank(message = "Le code est obligatoire") String code,
        @NotBlank(message = "Le libellé est obligatoire") String libelle
) implements FormRequest {
}
