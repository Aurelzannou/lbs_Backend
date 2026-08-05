package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerrouProgressionRequest implements FormRequest {
    @NotNull
    private Long classeId;
    @NotNull
    private Long matiereId;
    @NotNull
    private Long periodeId;
    /** "INTERROGATION" ou "DEVOIR" */
    @NotNull
    private String typeEvaluation;
    @NotNull
    private Integer numero;
}
