package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaiementRequest implements FormRequest {
    private String code;
    private String reference;
    private Long dossierEleveId;
    private Long fraisScolaireId;
    private LocalDate datePaiement;
    private Double montant;
    private Long modePaiementId;
    private Long caisseId;
    private Long utilisateurId;
    private String observation;
    private String canal;
    private String statutTransaction;
    private String telephonePaiement;

    @Override
    public String getCode() {
        return code;
    }
}
