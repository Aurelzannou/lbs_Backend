package com.App.lbs_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DossierEtapeResponse {
    private Long id;
    private String uuid;
    private Long dossierEleveId;
    private Long etapeId;
    private String etapeLibelle;
    private String statut;
    private LocalDateTime dateTraitement;
    private String commentaire;
    private Long utilisateurId;
    private String utilisateurNom;
    private LocalDateTime modifierLe;
    private String modifierPar;
}
