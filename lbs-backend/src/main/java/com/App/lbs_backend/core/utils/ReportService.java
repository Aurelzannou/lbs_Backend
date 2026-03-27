package com.App.lbs_backend.core.utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Service générique pour la génération de rapports via JasperReports.
 */
@Service
public class ReportService {

    /**
     * Génère un rapport au format PDF sous forme de tableau d'octets.
     *
     * @param reportName Nom du fichier .jrxml (sans extension) se trouvant dans src/main/resources/reports/
     * @param parameters Paramètres à injecter dans le rapport
     * @param data       Collection d'objets (DTO/Entity) pour alimenter le rapport
     * @return Contenu binaire du PDF
     * @throws Exception Si une erreur survient lors de la génération
     */
    public byte[] generatePdfReport(String reportName, Map<String, Object> parameters, Collection<?> data) throws Exception {
        // 1. Charger le fichier .jrxml
        InputStream reportStream = new ClassPathResource("reports/" + reportName + ".jrxml").getInputStream();

        // 2. Compiler le rapport
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // 3. Créer la source de données (si data n'est pas vide)
        JRDataSource dataSource = (data != null && !data.isEmpty()) 
                ? new JRBeanCollectionDataSource(data) 
                : new JREmptyDataSource();

        // 4. Remplir le rapport
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 5. Exporter en PDF
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
