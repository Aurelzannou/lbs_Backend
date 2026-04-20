package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfilRequest implements FormRequest {
    
    @NotBlank(message = "Le code est obligatoire")
    private String code;
    
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;
}
