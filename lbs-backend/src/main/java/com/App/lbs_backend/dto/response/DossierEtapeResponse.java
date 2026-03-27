package com.App.lbs_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DossierEtapeResponse {
    private Integer id;
    private String uuid;
    private Integer dossierEleveId;
    private Integer etapeId;
    private String etapeLibelle;
    private String statut;
    private LocalDateTime dateTraitement;
    private String commentaire;
    private Integer utilisateurId;
    private String utilisateurNom;
    private LocalDateTime modifierLe;
    private String modifierPar;
}
