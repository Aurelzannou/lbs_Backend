package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ClasseRequest implements FormRequest {
    @NotBlank(message = "Le code est obligatoire")
    @Size(max = 20, message = "Le code ne peut pas dépasser 20 caractères")
    private String code;

    @NotBlank(message = "Le libellé est obligatoire")
    @Size(max = 100, message = "Le libellé ne peut pas dépasser 100 caractères")
    private String libelle;

    @NotNull(message = "Le niveau est obligatoire")
    private Long niveauId;

    private Long profId;
    private Integer capaciteMax;
    private Boolean actif;

    /** Ancien format : liste d'ids de matières sans coefficient (conservé pour compatibilité). */
    private List<Long> matiereIds;

    /** Nouveau format : matière + coefficient, saisis directement sur le formulaire de la classe.
        Les coefficients sont enregistrés au niveau de la classe (référentiel Coefficient par niveau). */
    private List<MatiereCoefficientRequest> matieres;

    @Data
    public static class MatiereCoefficientRequest {
        private Long matiereId;
        private Double coefficient;
    }
}
