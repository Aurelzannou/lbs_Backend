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
}
