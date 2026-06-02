package com.App.lbs_backend.service;

import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportService {

    /**
     * Génère un PDF à partir d'un template .jrxml dans resources/reports/
     *
     * @param templateName  nom du fichier sans extension (ex: "fiche-inscription")
     * @param params        paramètres passés au rapport
     * @param data          liste de données (peut être vide si tout est dans les params)
     * @return byte[] du PDF généré
     */
    public byte[] generatePdf(String templateName, Map<String, Object> params, List<?> data) {
        try {
            // Charger le template .jrxml
            ClassPathResource resource = new ClassPathResource("reports/" + templateName + ".jrxml");
            InputStream inputStream = resource.getInputStream();

            // Compiler le template
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);

            // Source de données
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                data != null && !data.isEmpty() ? data : List.of(new Object())
            );

            // Paramètres
            Map<String, Object> allParams = new HashMap<>(params != null ? params : new HashMap<>());

            // Remplir et exporter en PDF
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, allParams, dataSource);
            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);

            log.info("PDF généré : {} ({} octets)", templateName, pdf.length);
            return pdf;

        } catch (Exception e) {
            log.error("Erreur génération PDF {} : {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du rapport PDF : " + e.getMessage(), e);
        }
    }
}
