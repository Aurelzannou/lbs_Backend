package com.App.lbs_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Une ligne du PDF « Liste des élèves » d'une classe (un élève).
 * Alimente le rapport JasperReports {@code reports/liste-eleves-classe.jrxml}.
 */
@Data
@AllArgsConstructor
public class EleveListeLignePdf {
    private Integer numero;
    private String nomComplet;
    private String sexe;
    private String dateNaissance;
}
