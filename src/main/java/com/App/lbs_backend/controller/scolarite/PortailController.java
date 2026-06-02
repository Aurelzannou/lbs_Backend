package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.mapper.TuteurMapper;
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

    private final TuteurRepository    tuteurRepository;
    private final TuteurMapper        tuteurMapper;
    private final DossierEleveService dossierEleveService;

    /** Retourne le profil du tuteur connecté (identifié via JWT). */
    @GetMapping("/me")
    public ResponseEntity<TuteurResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.notFound().build();
        String email = jwt.getClaimAsString("email");
        if (email == null) return ResponseEntity.notFound().build();

        return tuteurRepository.findByEmail(email)
                .map(t -> ResponseEntity.ok(tuteurMapper.toResponse(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Retourne les dossiers d'inscription du tuteur connecté. */
    @GetMapping("/mes-dossiers")
    public ResponseEntity<List<DossierEleveResponse>> getMesDossiers(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.ok(Collections.emptyList());
        String email = jwt.getClaimAsString("email");
        if (email == null) return ResponseEntity.ok(Collections.emptyList());

        return tuteurRepository.findByEmail(email)
                .map(t -> ResponseEntity.ok(dossierEleveService.getByTuteurId(t.getId())))
                .orElse(ResponseEntity.ok(Collections.emptyList()));
    }
}
