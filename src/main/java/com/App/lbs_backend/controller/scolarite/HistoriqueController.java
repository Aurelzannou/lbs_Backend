package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.service.scolarite.HistoriqueService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/historique")
@RequiredArgsConstructor
public class HistoriqueController {

    private final HistoriqueService  historiqueService;
    private final HttpServletRequest httpRequest;

    @GetMapping("/dossiers/{uuid}")
    public ResponseEntity<?> getByDossier(@PathVariable String uuid) {
        return ResponseEntity.ok(
            ApiResponse.apiSuccess("OK",
                historiqueService.getByDossierUuid(uuid),
                httpRequest.getRequestURI())
        );
    }
}
