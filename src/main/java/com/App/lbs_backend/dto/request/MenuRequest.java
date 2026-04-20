package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class MenuRequest implements FormRequest {
    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    // path est optionnel : les menus-groupes parents n'ont pas de route
    private String path;

    @NotNull(message = "L'ordre est obligatoire")
    private Integer ordre;

    private Long menuEnfantId; // Utilisé pour désigner le parent

    private List<Long> profilIds;
}
