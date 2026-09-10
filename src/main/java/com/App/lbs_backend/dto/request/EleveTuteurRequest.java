package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EleveTuteurRequest implements FormRequest {

    @NotNull(message = "L'élève est obligatoire")
    private Long eleveId;

    @NotNull(message = "Le parent est obligatoire")
    private Long tuteurId;

    /** Père / Mère / Tuteur / Autre — libre. */
    private String lienParente;

    private Boolean contactUrgence;
}
