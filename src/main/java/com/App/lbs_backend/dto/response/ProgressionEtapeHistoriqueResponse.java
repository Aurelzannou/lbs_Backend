package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProgressionEtapeHistoriqueResponse {
    private String etape;
    private String etapeLibelle;
    private LocalDateTime dateTransition;
    private String auteurEmail;
}
