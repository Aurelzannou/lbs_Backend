package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ValidationBulletinResponse {
    private Long classeId;
    private String classeLibelle;
    private Long periodeId;
    private Long anneeScolaireId;
    private boolean valide;
    private LocalDateTime dateValidation;
    private String valideParEmail;

    /** Avancement des professeurs : matières envoyées à l'administration / total des matières de la classe. */
    private int matieresRecues;
    private int matieresTotal;
}
