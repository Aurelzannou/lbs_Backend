package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EcheancierRequest implements FormRequest {
    @NotNull(message = "Le frais scolaire est obligatoire")
    private Long fraisScolaireId;

    @NotNull(message = "Le numéro de tranche est obligatoire")
    private Integer numero;

    private String libelle;

    @NotNull(message = "La date d'échéance est obligatoire")
    private LocalDate dateEcheance;

    @NotNull(message = "Le montant est obligatoire")
    private Double montant;
}
