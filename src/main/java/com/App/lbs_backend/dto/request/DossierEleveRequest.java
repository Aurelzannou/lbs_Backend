package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DossierEleveRequest implements FormRequest {
    private String code;
    private Long eleveId;
    private Long classeId;
    private Long anneeScolaireId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long statutId;
    private Long etapeCouranteId;
    private Double remise;
    private String numero;
    private Long typeOperationId;
    private Long acteId;
    private Long utilisateurId;

    @Override
    public String getCode() {
        return code;
    }
}
