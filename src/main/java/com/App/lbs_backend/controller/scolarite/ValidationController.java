package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.service.scolarite.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PutMapping("/dossiers/{uuid}/accepter")
    public ResponseEntity<DossierEleveResponse> accepter(@PathVariable String uuid) {
        return ResponseEntity.ok(validationService.accepter(uuid));
    }

    @PutMapping("/dossiers/{uuid}/refuser")
    public ResponseEntity<DossierEleveResponse> refuser(
            @PathVariable String uuid,
            @RequestBody(required = false) Map<String, String> body) {
        String motif = body != null ? body.get("motif") : null;
        return ResponseEntity.ok(validationService.refuser(uuid, motif));
    }

    @PutMapping("/dossiers/{uuid}/inscrire")
    public ResponseEntity<DossierEleveResponse> inscrire(@PathVariable String uuid) {
        return ResponseEntity.ok(validationService.inscrire(uuid));
    }

    @GetMapping("/mes-dossiers")
    public ResponseEntity<List<DossierEleveResponse>> getMesDossiers(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.ok(Collections.emptyList());
        String email = jwt.getClaimAsString("email");
        if (email == null) return ResponseEntity.ok(Collections.emptyList());
        return ResponseEntity.ok(validationService.getMesDossiers(email));
    }
}
