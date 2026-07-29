package com.App.lbs_backend.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FeuillePresenceRequest {
    private Long emploiTempsId;
    private LocalDate date;
    private String profStatut;
    private List<EleveStatut> eleves;

    @Data
    public static class EleveStatut {
        private Long eleveId;
        private String statut;
    }
}
