package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.response.SuiviGlobalLigneResponse;
import com.App.lbs_backend.dto.response.SuiviPaiementResponse;
import com.App.lbs_backend.service.scolarite.SuiviPaiementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suivi-paiements")
@RequiredArgsConstructor
public class SuiviPaiementController {

    private final SuiviPaiementService suiviPaiementService;
    private final HttpServletRequest request;

    @GetMapping("/dossier/{dossierEleveId}")
    public ResponseEntity<?> getSuiviParDossier(@PathVariable Long dossierEleveId) {
        SuiviPaiementResponse dto = suiviPaiementService.getSuiviParDossier(dossierEleveId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", dto, request.getRequestURI()));
    }

    /** Vue globale « Tous les impayés », toutes années confondues par défaut. */
    @GetMapping("/impayes")
    public ResponseEntity<?> listerImpayes(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) String statut) {
        List<SuiviGlobalLigneResponse> resultat = suiviPaiementService.listerImpayes(anneeScolaireId, classeId, statut);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", resultat, request.getRequestURI()));
    }

    @GetMapping("/impayes/pdf")
    public ResponseEntity<byte[]> exporterImpayesPdf(
            @RequestParam(required = false) Long anneeScolaireId,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) String statut) {
        byte[] pdf = suiviPaiementService.genererImpayesPdf(anneeScolaireId, classeId, statut);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"liste-impayes.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
