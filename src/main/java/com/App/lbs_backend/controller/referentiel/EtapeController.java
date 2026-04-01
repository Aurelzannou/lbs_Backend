package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.EtapeRequest;
import com.App.lbs_backend.dto.response.EtapeResponse;
import com.App.lbs_backend.service.referentiel.EtapeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/etapes")
@RequiredArgsConstructor
public class EtapeController {

    private final EtapeService etapeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EtapeResponse>>> getAll(HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Liste des étapes récupérée avec succès",
                etapeService.getAll(),
                req.getRequestURI()
        ));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<EtapeResponse>> getByUuid(@PathVariable String uuid, HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Étape récupérée avec succès",
                etapeService.getByUuid(uuid),
                req.getRequestURI()
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EtapeResponse>> create(@Valid @RequestBody EtapeRequest request, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.apiSuccess(
                "Étape créée avec succès",
                etapeService.create(request),
                req.getRequestURI()
        ));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<EtapeResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody EtapeRequest request,
            HttpServletRequest req) {
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Étape modifiée avec succès",
                etapeService.update(uuid, request),
                req.getRequestURI()
        ));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String uuid, HttpServletRequest req) {
        etapeService.delete(uuid);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Étape supprimée avec succès", null, req.getRequestURI()));
    }
}
