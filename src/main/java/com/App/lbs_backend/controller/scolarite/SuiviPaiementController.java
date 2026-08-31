package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.response.SuiviPaiementResponse;
import com.App.lbs_backend.service.scolarite.SuiviPaiementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
