package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.mapper.TuteurMapper;
import com.App.lbs_backend.repository.TuteurRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints publics du portail parent.
 * Responsabilité unique : profil du tuteur connecté.
 * Les dossiers sont gérés par ValidationController (/api/validation/mes-dossiers).
 */
@RestController
@RequestMapping("/api/portail")
@RequiredArgsConstructor
public class PortailController {

    private final TuteurRepository  tuteurRepository;
    private final TuteurMapper      tuteurMapper;
    private final HttpServletRequest httpRequest;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return ResponseEntity.notFound().build();
        String email = jwt.getClaimAsString("email");
        if (email == null) email = jwt.getClaimAsString("preferred_username");
        if (email == null) return ResponseEntity.notFound().build();

        return tuteurRepository.findByEmail(email)
                .map(t -> ResponseEntity.ok(
                        ApiResponse.apiSuccess("Profil récupéré", tuteurMapper.toResponse(t), httpRequest.getRequestURI())))
                .orElse(ResponseEntity.notFound().build());
    }
}
