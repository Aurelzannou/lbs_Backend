package com.App.lbs_backend.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DossierEleveResponse {
    private Long id;
    private String uuid;
    private String code;
    private Long eleveId;
    private String eleveNom;
    private String elevePrenom;
    private Long classeId;
    private String classeLibelle;
    private Long anneeScolaireId;
    private String anneeScolaireLibelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long statutId;
    private String statutLibelle;
    private Long etapeCouranteId;
    private String etapeCouranteLibelle;
    private Double remise;
    private String numero;
    private Long typeOperationId;
    private String typeOperationLibelle;
    private Long acteId;
    private Long utilisateurId;
}
