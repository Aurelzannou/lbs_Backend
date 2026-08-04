package com.App.lbs_backend.core.utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
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
        JasperPrint jasperPrint = fillReport(reportName, parameters, data);
        return exportToPdf(List.of(jasperPrint));
    }

    /** Compile et remplit un rapport sans l'exporter — utile pour fusionner plusieurs rapports
        (ex: un bulletin par élève d'une classe) en un seul PDF via {@link #exportToPdf}. */
    public JasperPrint fillReport(String reportName, Map<String, Object> parameters, Collection<?> data) throws Exception {
        InputStream reportStream = new ClassPathResource("reports/" + reportName + ".jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        JRDataSource dataSource = (data != null && !data.isEmpty())
                ? new JRBeanCollectionDataSource(data)
                : new JREmptyDataSource();

        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }

    /** Exporte une liste de rapports déjà remplis en un seul PDF (un rapport par page/groupe). */
    public byte[] exportToPdf(List<JasperPrint> jasperPrints) throws Exception {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrints));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        exporter.exportReport();
        return out.toByteArray();
    }
}
