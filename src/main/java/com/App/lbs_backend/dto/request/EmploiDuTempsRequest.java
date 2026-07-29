package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class EmploiDuTempsRequest implements FormRequest {
    @NotNull(message = "La classe est obligatoire")
    private Long classeId;

    @NotNull(message = "L'année scolaire est obligatoire")
    private Long anneeScolaireId;

    @NotNull(message = "La matière est obligatoire")
    private Long matiereId;

    @NotNull(message = "Le professeur est obligatoire")
    private Long profId;

    @NotNull(message = "Le jour est obligatoire")
    private String jour;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;
}
