package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.service.scolarite.ValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService  validationService;
    private final HttpServletRequest httpRequest;

    @PutMapping("/dossiers/{uuid}/accepter")
    public ResponseEntity<?> accepter(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.apiSuccess("Dossier accepté", validationService.accepter(uuid), httpRequest.getRequestURI()));
    }

    @PutMapping("/dossiers/{uuid}/refuser")
    public ResponseEntity<?> refuser(
            @PathVariable String uuid,
            @RequestBody(required = false) Map<String, String> body) {
        String motif = body != null ? body.get("motif") : null;
        return ResponseEntity.ok(ApiResponse.apiSuccess("Dossier refusé", validationService.refuser(uuid, motif), httpRequest.getRequestURI()));
    }

    @PutMapping("/dossiers/{uuid}/inscrire")
    public ResponseEntity<?> inscrire(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.apiSuccess("Élève inscrit", validationService.inscrire(uuid), httpRequest.getRequestURI()));
    }

    @GetMapping("/dossiers")
    public ResponseEntity<?> listerDossiers(
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<DossierEleveResponse> dossiers = validationService.listerDossiers(
                Optional.ofNullable(statut), page, size);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", dossiers, httpRequest.getRequestURI()));
    }

    @GetMapping("/mes-dossiers")
    public ResponseEntity<?> getMesDossiers(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.ok(ApiResponse.apiSuccess("OK", Collections.emptyList(), httpRequest.getRequestURI()));
        // preferred_username = email dans ce système (email utilisé comme username Keycloak)
        String email = jwt.getClaimAsString("email");
        if (email == null) email = jwt.getClaimAsString("preferred_username");
        if (email == null) return ResponseEntity.ok(ApiResponse.apiSuccess("OK", Collections.emptyList(), httpRequest.getRequestURI()));
        List<DossierEleveResponse> dossiers = validationService.getMesDossiers(email);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", dossiers, httpRequest.getRequestURI()));
    }
}
