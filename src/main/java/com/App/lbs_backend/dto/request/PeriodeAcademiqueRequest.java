package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PeriodeAcademiqueRequest implements FormRequest {
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    private String code;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 150, message = "Le libellé ne peut pas dépasser 150 caractères")
    private String libelle;

    @NotNull(message = "L'année scolaire est obligatoire")
    private Long anneeScolaireId;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Boolean verrouille;
}
