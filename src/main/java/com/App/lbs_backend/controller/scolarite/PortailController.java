package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.service.scolarite.DossierEleveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/portail")
@RequiredArgsConstructor
public class PortailController {

    private final TuteurRepository       tuteurRepository;
    private final DossierEleveService    dossierEleveService;

    /**
     * Retourne les dossiers d'inscription du tuteur actuellement connecté.
     * L'identité est extraite directement depuis le JWT Keycloak.
     */
    @GetMapping("/mes-dossiers")
    public ResponseEntity<List<DossierEleveResponse>> getMesDossiers(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.ok(Collections.emptyList());

        // Récupérer l'email depuis le JWT
        String email = jwt.getClaimAsString("email");
        if (email == null) return ResponseEntity.ok(Collections.emptyList());

        return tuteurRepository.findByEmail(email)
                .map(tuteur -> ResponseEntity.ok(dossierEleveService.getByTuteurId(tuteur.getId())))
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }
}
