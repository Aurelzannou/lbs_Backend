package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProgressionMatiereRequest implements FormRequest {
    @NotNull
    private Long classeId;
    @NotNull
    private Long matiereId;
    @NotNull
    private Long periodeId;

    /** Envoi sélectif : figer les interrogations / devoirs jusqu'à ce numéro inclus.
        null = figer toutes les colonnes qui portent une note. */
    private Integer interrogationsJusqua;
    private Integer devoirsJusqua;
}
