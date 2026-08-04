package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.FeuilleSaisieNotesRequest;
import com.App.lbs_backend.dto.response.ClasseMatiereANoterResponse;
import com.App.lbs_backend.dto.response.FeuilleSaisieNotesResponse;
import com.App.lbs_backend.repository.ProfesseurRepository;
import com.App.lbs_backend.service.scolarite.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final ProfesseurRepository professeurRepository;
    private final HttpServletRequest httpRequest;

    @GetMapping("/feuille")
    public ResponseEntity<?> getFeuille(
            @RequestParam Long classeId, @RequestParam Long matiereId, @RequestParam Long periodeId) {
        FeuilleSaisieNotesResponse feuille = noteService.getFeuille(classeId, matiereId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", feuille, httpRequest.getRequestURI()));
    }

    @PutMapping("/feuille")
    public ResponseEntity<?> enregistrerFeuille(
            @RequestBody FeuilleSaisieNotesRequest form, @AuthenticationPrincipal Jwt jwt) {
        // Si l'appelant est un professeur, on ne fait jamais confiance au professeurId envoyé par
        // le client pour l'autorisation — on le re-dérive du JWT connecté.
        String email = extraireEmail(jwt);
        if (email != null) {
            professeurRepository.findByEmail(email).ifPresent(p -> form.setProfesseurId(p.getId()));
        }
        FeuilleSaisieNotesResponse feuille = noteService.enregistrerFeuille(form);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Notes enregistrées", feuille, httpRequest.getRequestURI()));
    }

    @GetMapping("/mes-classes")
    public ResponseEntity<?> getMesClasses(@AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        if (email == null) return ResponseEntity.ok(ApiResponse.apiSuccess("OK", Collections.emptyList(), httpRequest.getRequestURI()));

        List<ClasseMatiereANoterResponse> classes = professeurRepository.findByEmail(email)
                .map(p -> noteService.getMesClassesANoter(p.getId()))
                .orElse(Collections.emptyList());
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", classes, httpRequest.getRequestURI()));
    }

    private String extraireEmail(Jwt jwt) {
        if (jwt == null) return null;
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getClaimAsString("preferred_username");
    }
}
