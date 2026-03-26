package com.App.lbs_backend.controller;

import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.mapper.UtilisateurMapper;
import com.App.lbs_backend.service.UtilisateurSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UtilisateurSyncService utilisateurSyncService;
    private final UtilisateurMapper utilisateurMapper;

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
     * Endpoint public de test (défini sans sécurité dans SecurityConfig)
     */
    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        return ResponseEntity.ok("✅ API publique accessible sans token");
    }
}
