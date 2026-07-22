package com.App.lbs_backend.controller;

import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.mapper.UtilisateurMapper;
import com.App.lbs_backend.service.KeycloakAdminService;
import com.App.lbs_backend.service.UtilisateurSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.App.lbs_backend.dto.request.RegisterRequest;
import com.App.lbs_backend.service.AuthService;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurSyncService utilisateurSyncService;
    private final UtilisateurMapper utilisateurMapper;
    private final AuthService authService;
    private final KeycloakAdminService keycloakAdminService;

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
     * Indique si un compte a déjà configuré l'OTP (TOTP), pour permettre au front de savoir
     * s'il doit demander un code de vérification après un premier échec de connexion.
     * N'expose aucune information sur l'existence du mot de passe — uniquement sur l'OTP.
     */
    @GetMapping("/otp-required")
    public ResponseEntity<Map<String, Boolean>> isOtpRequired(@RequestParam String username) {
        boolean otpRequired = keycloakAdminService.hasOtpConfigured(username);
        return ResponseEntity.ok(Map.of("otpRequired", otpRequired));
    }

    /**
     * Endpoint public de test (défini sans sécurité dans SecurityConfig)
     */
    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        return ResponseEntity.ok("✅ API publique accessible sans token");
    }
}
