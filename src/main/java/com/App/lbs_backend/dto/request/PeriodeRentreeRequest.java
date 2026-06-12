package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PeriodeRentreeRequest implements FormRequest {

    @NotNull(message = "L'année scolaire est obligatoire")
    private Long anneeScolaireId;

    private String libelle;

    @NotNull(message = "La date d'ouverture est obligatoire")
    private LocalDate dateOuverture;

    @NotNull(message = "La date de clôture est obligatoire")
    private LocalDate dateCloture;

    private Boolean actif;
}
