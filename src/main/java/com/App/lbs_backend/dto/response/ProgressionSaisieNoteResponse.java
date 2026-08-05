package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProgressionSaisieNoteResponse {
    private Long classeId;
    private Long matiereId;
    private Long periodeId;
    private int interrogationsVerroueesJusqua;
    private int devoirsVerrouesJusqua;
    private int interrogationsValideesJusqua;
    private int devoirsValideesJusqua;
    /** BROUILLON / SOUMISE / VALIDEE */
    private String etape;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateValidation;
    private String valideParEmail;
}
