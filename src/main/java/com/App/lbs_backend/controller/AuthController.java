package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.ActivationCompteRequest;
import com.App.lbs_backend.dto.request.ChangePasswordRequest;
import com.App.lbs_backend.dto.request.ForgotPasswordRequest;
import com.App.lbs_backend.dto.request.ResetPasswordRequest;
import com.App.lbs_backend.service.PasswordResetService;
import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.mapper.UtilisateurMapper;
import com.App.lbs_backend.service.KeycloakAdminService;
import com.App.lbs_backend.service.UtilisateurSyncService;
import com.App.lbs_backend.service.referentiel.ProfesseurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final ProfesseurService professeurService;
    private final PasswordResetService passwordResetService;

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
     * Changement de mot de passe par l'utilisateur connecté (depuis l'écran « Mon Profil »).
     * Nécessite un JWT valide ; l'ancien mot de passe est vérifié avant application.
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        authService.changePassword(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                request.getCurrentPassword(),
                request.getNewPassword());
        return ResponseEntity.ok(
                ApiResponse.apiSuccess("Votre mot de passe a été modifié avec succès.", null,
                        "/api/auth/change-password"));
    }

    /**
     * Étape 1 du parcours « Mot de passe oublié » : envoie (si le compte existe) un lien de
     * réinitialisation par email. Réponse volontairement identique que le compte existe ou non.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.demanderReinitialisation(request.getEmail());
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Si un compte est associé à cette adresse, un email de réinitialisation vient d'être envoyé.",
                null, "/api/auth/forgot-password"));
    }

    /**
     * Étape 2 du parcours « Mot de passe oublié » : consomme le jeton reçu par email et applique
     * le nouveau mot de passe choisi par l'utilisateur.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reinitialiser(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.apiSuccess(
                "Votre mot de passe a été réinitialisé. Vous pouvez maintenant vous connecter.",
                null, "/api/auth/reset-password"));
    }

    /**
     * Endpoint public consommant un lien d'activation de compte professeur : le professeur
     * choisit lui-même son mot de passe, jamais transmis en clair par email.
     */
    @PostMapping("/activer-compte-professeur")
    public ResponseEntity<String> activerCompteProfesseur(@Valid @RequestBody ActivationCompteRequest request) {
        professeurService.activerCompte(request.getToken(), request.getMotDePasse());
        return ResponseEntity.ok("Compte activé avec succès. Vous pouvez maintenant vous connecter.");
    }

    /**
     * Endpoint public de test (défini sans sécurité dans SecurityConfig)
     */
    @GetMapping("/test")
    public ResponseEntity<String> testPublicEndpoint() {
        return ResponseEntity.ok("✅ API publique accessible sans token");
    }
}
