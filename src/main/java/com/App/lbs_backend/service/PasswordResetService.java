package com.App.lbs_backend.service;

import com.App.lbs_backend.entity.PasswordResetToken;
import com.App.lbs_backend.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Parcours « Mot de passe oublié », valable pour tous les types de comptes (admin, tuteur,
 * professeur) puisqu'il s'appuie sur l'identifiant Keycloak et non sur une entité métier.
 *
 * <p>Sécurité : {@link #demanderReinitialisation(String)} ne révèle jamais si un compte existe
 * (réponse identique dans tous les cas). Le mot de passe n'est jamais transmis par email :
 * l'utilisateur le choisit lui-même via le lien.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int VALIDITE_MINUTES = 60;

    private final PasswordResetTokenRepository tokenRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /** Émet (si le compte existe) un lien de réinitialisation à usage unique valable 1 heure.
        Ne signale jamais l'absence de compte à l'appelant. */
    @Transactional
    public void demanderReinitialisation(String email) {
        String login = email == null ? "" : email.trim().toLowerCase();
        UserRepresentation user = keycloakAdminService.findLoginRepresentation(login);

        if (user == null) {
            log.info("Demande de réinitialisation pour un compte inconnu ({}) — ignorée en silence.", login);
            return;
        }
        if (Boolean.FALSE.equals(user.isEnabled())) {
            log.info("Demande de réinitialisation pour un compte désactivé ({}) — ignorée.", login);
            return;
        }

        // L'email part vers l'adresse réelle du compte Keycloak, pas vers ce que l'utilisateur
        // a tapé (il a pu se connecter avec un username comme « superadmin »).
        String destinataire = user.getEmail() != null && !user.getEmail().isBlank()
                ? user.getEmail() : login;

        tokenRepository.invaliderJetonsActifs(user.getId());

        PasswordResetToken jeton = new PasswordResetToken(
                UUID.randomUUID().toString(),
                user.getId(),
                destinataire,
                LocalDateTime.now().plusMinutes(VALIDITE_MINUTES));
        tokenRepository.save(jeton);

        String lien = frontendUrl + "/reinitialiser-mot-de-passe?token=" + jeton.getToken();
        emailService.sendPasswordReset(destinataire, user.getFirstName(), lien);
        log.info("Lien de réinitialisation envoyé à {}", destinataire);
    }

    /** Consomme le jeton : applique le mot de passe choisi et invalide le lien. */
    @Transactional
    public void reinitialiser(String token, String nouveauMotDePasse) {
        PasswordResetToken jeton = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ce lien de réinitialisation est invalide ou a déjà été utilisé."));

        if (!jeton.estValide()) {
            throw new IllegalArgumentException(
                    "Ce lien de réinitialisation a expiré. Refaites une demande depuis la page de connexion.");
        }

        keycloakAdminService.resetPasswordAndEnableLogin(jeton.getKeycloakId(), nouveauMotDePasse);

        jeton.setUtilise(true);
        tokenRepository.save(jeton);
        log.info("Mot de passe réinitialisé pour {}", jeton.getEmail());
    }
}
