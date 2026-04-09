package com.App.lbs_backend.controller;

import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.mapper.UtilisateurMapper;
import com.App.lbs_backend.service.UtilisateurSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.App.lbs_backend.dto.request.RegisterRequest;
import com.App.lbs_backend.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurSyncService utilisateurSyncService;
    private final UtilisateurMapper utilisateurMapper;
    private final AuthService authService;

    /**
     * Endpoint permettant à l'application Front-End de récupérer les 
     * informations du profil actuellement connecté.
     */
    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponse> getCurrentUser() {
        var user = utilisateurSyncService.getCurrentUser();
        return ResponseEntity.ok(utilisateurMapper.toResponse(user));
    }

    /**
     * Endpoint public pour l'inscription d'un nouvel utilisateur.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Inscription réussie. Vous pouvez maintenant vous connecter.");
    }

    /**
     * Endpoint public de test (défini sans sécurité dans SecurityConfig)
     */
    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        return ResponseEntity.ok("✅ API publique accessible sans token");
    }
}
