package com.App.lbs_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Une ligne du PDF « Liste des impayés ». Alimente le rapport JasperReports
 * {@code reports/liste-impayes.jrxml}.
 */
@Data
@AllArgsConstructor
public class ImpayeLignePdf {
    private String eleve;
    private String classe;
    private String anneeScolaire;
    private String totalDu;
    private String totalPaye;
    private String totalReste;
    private String statut;
}
