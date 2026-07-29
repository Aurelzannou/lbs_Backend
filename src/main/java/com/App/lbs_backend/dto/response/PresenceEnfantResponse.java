package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PresenceEnfantResponse {
    private Long eleveId;
    private String eleveNomComplet;
    private List<PresenceHistoriqueResponse> historique;
}
