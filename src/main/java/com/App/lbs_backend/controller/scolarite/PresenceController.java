package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.FeuillePresenceRequest;
import com.App.lbs_backend.dto.response.FeuillePresenceResponse;
import com.App.lbs_backend.dto.response.PresenceEnfantResponse;
import com.App.lbs_backend.dto.response.SeanceJourResponse;
import com.App.lbs_backend.service.scolarite.PresenceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/presences")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;
    private final HttpServletRequest request;

    @GetMapping("/jour")
    public ResponseEntity<?> getJour(
            @RequestParam Long classeId,
            @RequestParam Long anneeScolaireId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SeanceJourResponse> result = presenceService.getSeancesDuJour(classeId, anneeScolaireId, date);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", result, request.getRequestURI()));
    }

    @GetMapping("/feuille")
    public ResponseEntity<?> getFeuille(
            @RequestParam Long emploiTempsId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        FeuillePresenceResponse result = presenceService.getFeuille(emploiTempsId, date);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", result, request.getRequestURI()));
    }

    @PutMapping("/feuille")
    public ResponseEntity<?> enregistrerFeuille(@RequestBody FeuillePresenceRequest form) {
        FeuillePresenceResponse result = presenceService.enregistrerFeuille(form);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Présence enregistrée", result, request.getRequestURI()));
    }

    /** Historique de présence des enfants du tuteur connecté — portail parent. */
    @GetMapping("/mes-enfants")
    public ResponseEntity<?> getMesEnfants(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.ok(ApiResponse.apiSuccess("OK", Collections.emptyList(), request.getRequestURI()));
        String email = jwt.getClaimAsString("email");
        if (email == null) email = jwt.getClaimAsString("preferred_username");
        if (email == null) return ResponseEntity.ok(ApiResponse.apiSuccess("OK", Collections.emptyList(), request.getRequestURI()));

        List<PresenceEnfantResponse> result = presenceService.getPresencesMesEnfants(email);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", result, request.getRequestURI()));
    }
}
