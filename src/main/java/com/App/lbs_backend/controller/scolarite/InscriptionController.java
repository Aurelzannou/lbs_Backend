package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.SoumettreInscriptionRequest;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.service.scolarite.InscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscription")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;
    private final TuteurRepository   tuteurRepository;
    private final HttpServletRequest  httpRequest;

    @PostMapping("/soumettre")
    public ResponseEntity<?> soumettre(
            @RequestBody SoumettreInscriptionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        // Garantit que tuteurId est toujours résolu depuis le JWT,
        // même si le frontend n'a pas pu charger le profil tuteur.
        if (request.getTuteurId() == null && jwt != null) {
            String email = jwt.getClaimAsString("email");
            if (email != null) {
                tuteurRepository.findByEmail(email)
                        .ifPresent(t -> request.setTuteurId(t.getId()));
            }
        }

        DossierEleveResponse dossier = inscriptionService.soumettre(request);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Inscription soumise avec succès", dossier, httpRequest.getRequestURI()));
    }
}
