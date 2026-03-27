package com.App.lbs_backend.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DossierEleveResponse {
    private Integer id;
    private String uuid;
    private String code;
    private Integer eleveId;
    private String eleveNom;
    private String elevePrenom;
    private Integer classeId;
    private String classeLibelle;
    private Integer anneeScolaireId;
    private String anneeScolaireLibelle;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer statutId;
    private String statutLibelle;
    private Integer etapeCouranteId;
    private String etapeCouranteLibelle;
    private Double remise;
    private String numero;
    private Integer typeOperationId;
    private String typeOperationLibelle;
    private Integer acteId;
    private Integer utilisateurId;
}
