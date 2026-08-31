package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DepenseScolaireRequest implements FormRequest {
    private String reference;

    @NotNull(message = "La caisse est obligatoire")
    private Long caisseId;

    @NotNull(message = "La catégorie de dépense est obligatoire")
    private Long categorieDepenseId;

    @NotNull(message = "Le montant est obligatoire")
    private Double montant;

    @NotNull(message = "La date de dépense est obligatoire")
    private LocalDate dateDepense;

    @NotBlank(message = "Le motif est obligatoire")
    private String motif;

    private Long utilisateurId;
}
