package com.App.lbs_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Une ligne du brouillon PDF de l'emploi du temps d'une classe (une séance de cours).
 * Alimente le rapport JasperReports {@code reports/emploi-du-temps.jrxml}.
 */
@Data
@AllArgsConstructor
public class EmploiDuTempsLignePdf {
    private String jour;
    private String horaire;
    private String matiere;
    private String professeur;
}
