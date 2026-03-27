package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.utils.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-report")
@RequiredArgsConstructor
public class TestReportController {

    private final ReportService reportService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> testPdf() {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("title", "REÇU DE TEST LBS");
            parameters.put("studentName", "Jean Dupont");
            parameters.put("amount", 50000.0);

            // Pour ce test simple, on passe une liste vide car on utilise uniquement des paramètres
            byte[] reportContent = reportService.generatePdfReport("recu_test", parameters, List.of());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recu_test.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(reportContent);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
