package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.DossierEtapeRequest;
import com.App.lbs_backend.dto.response.DossierEtapeResponse;
import com.App.lbs_backend.service.DossierEtapeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/dossier-etapes-historique")
@RequiredArgsConstructor
public class DossierEtapeController {

    private final DossierEtapeService dossierEtapeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DossierEtapeResponse>>> getAll(HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Historique des étapes récupéré avec succès",
                dossierEtapeService.getAll(),
                req.getRequestURI()
        ));
    }

    @GetMapping("/dossier/{dossierEleveId}")
    public ResponseEntity<ApiResponse<List<DossierEtapeResponse>>> getByDossierId(@PathVariable Integer dossierEleveId, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Historique des étapes du dossier récupéré avec succès",
                dossierEtapeService.getByDossierEleveId(dossierEleveId),
                req.getRequestURI()
        ));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<DossierEtapeResponse>> getByUuid(@PathVariable String uuid, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Historique d'étape récupéré avec succès",
                dossierEtapeService.getByUuid(uuid),
                req.getRequestURI()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DossierEtapeResponse>> create(@Valid @RequestBody DossierEtapeRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.apiSuccess(
                "Étape franchie ajoutée avec succès",
                dossierEtapeService.create(request),
                req.getRequestURI()
        ));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<DossierEtapeResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody DossierEtapeRequest request,
            HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Historique d'étape modifié avec succès",
                dossierEtapeService.update(uuid, request),
                req.getRequestURI()
        ));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String uuid, HttpServletRequest req) {
        dossierEtapeService.delete(uuid);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Historique d'étape supprimé avec succès", null, req.getRequestURI()));
    }
}
