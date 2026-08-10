package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.FeuilleSaisieNotesRequest;
import com.App.lbs_backend.dto.response.FeuilleSaisieNotesResponse;
import com.App.lbs_backend.dto.response.ValidationBulletinResponse;
import com.App.lbs_backend.service.scolarite.ValidationBulletinService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validation-bulletins")
@RequiredArgsConstructor
public class ValidationBulletinController {

    private final ValidationBulletinService validationBulletinService;
    private final HttpServletRequest httpRequest;

    @GetMapping("/statut")
    public ResponseEntity<?> getStatut(@RequestParam Long classeId, @RequestParam Long periodeId) {
        ValidationBulletinResponse statut = validationBulletinService.getStatut(classeId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", statut, httpRequest.getRequestURI()));
    }

    @GetMapping
    public ResponseEntity<?> lister(@RequestParam Long periodeId, @RequestParam Long anneeScolaireId) {
        List<ValidationBulletinResponse> liste = validationBulletinService.listerParPeriode(periodeId, anneeScolaireId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", liste, httpRequest.getRequestURI()));
    }

    @PutMapping("/{classeId}/{periodeId}/valider")
    public ResponseEntity<?> valider(
            @PathVariable Long classeId,
            @PathVariable Long periodeId,
            @RequestParam Long anneeScolaireId,
            @AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        ValidationBulletinResponse resultat = validationBulletinService.valider(classeId, periodeId, anneeScolaireId, email);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Bulletins validés", resultat, httpRequest.getRequestURI()));
    }

    /** Enregistre des notes corrigées depuis l'écran "Validation des bulletins" — service à part
        entière (voir ValidationBulletinService.corrigerNotes), distinct de PUT /api/notes/feuille
        qui reste réservé à la saisie (professeur + écran admin "Saisie des notes"). */
    @PutMapping("/notes")
    public ResponseEntity<?> corrigerNotes(@RequestBody FeuilleSaisieNotesRequest form) {
        FeuilleSaisieNotesResponse dto = validationBulletinService.corrigerNotes(form);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Notes mises à jour", dto, httpRequest.getRequestURI()));
    }

    @PutMapping("/{classeId}/{periodeId}/devalider")
    public ResponseEntity<?> devalider(@PathVariable Long classeId, @PathVariable Long periodeId) {
        ValidationBulletinResponse resultat = validationBulletinService.devalider(classeId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Validation annulée", resultat, httpRequest.getRequestURI()));
    }

    private String extraireEmail(Jwt jwt) {
        if (jwt == null) return null;
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getClaimAsString("preferred_username");
    }
}
