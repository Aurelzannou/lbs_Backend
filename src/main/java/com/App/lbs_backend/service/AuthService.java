package com.App.lbs_backend.service;

import com.App.lbs_backend.dto.request.RegisterRequest;
import com.App.lbs_backend.entity.Profil;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.repository.ProfilRepository;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KeycloakAdminService keycloakAdminService;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final TuteurRepository tuteurRepository;

    /**
     * Inscription publique : crée toujours un compte TUTEUR (Tuteur + Utilisateur compagnon).
     * La création de comptes ADMIN se fait exclusivement via l'attribution de profils
     * par un administrateur déjà authentifié (voir updateUserProfils), jamais via cet endpoint public.
     */
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Inscription parent d'élève : {}", request.getEmail());

        if (tuteurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée.");
        }
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée.");
        }

        // Pour les parents, l'email sert d'identifiant Keycloak
        String keycloakId = keycloakAdminService.createUser(
                request.getEmail(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
        );

        Tuteur tuteur = new Tuteur();
        tuteur.setKeycloakId(keycloakId);
        tuteur.setNom(request.getLastName());
        tuteur.setPrenom(request.getFirstName());
        tuteur.setEmail(request.getEmail());
        tuteur.setTelephone1(request.getTelephone());
        tuteur.setActif(true);
        tuteurRepository.save(tuteur);

        // Utilisateur compagnon : permet à un admin d'attribuer d'autres profils plus tard
        // via l'écran d'attribution de profils (opère uniquement sur Utilisateur).
        Utilisateur user = new Utilisateur();
        user.setKeycloack(keycloakId);
        user.setLogin(request.getEmail());
        user.setEmail(request.getEmail());
        user.setNom(request.getLastName());
        user.setPrenom(request.getFirstName());
        utilisateurRepository.save(user);

        profilRepository.findByCode("TUTEUR").ifPresentOrElse(profil -> {
            assignProfilToUser(user, profil);
            try {
                keycloakAdminService.assignRoleToUser(keycloakId, profil.getCode());
            } catch (Exception e) {
                log.error("Erreur assignation rôle {} dans Keycloak : {}", profil.getCode(), e.getMessage());
            }
        }, () -> log.warn("Profil 'TUTEUR' non trouvé."));

        log.info("Parent d'élève enregistré avec succès : {}", request.getEmail());
    }

    /**
     * Changement de mot de passe par l'utilisateur connecté depuis son profil.
     * L'ancien mot de passe est vérifié auprès de Keycloak avant d'appliquer le nouveau,
     * ce qui évite d'exposer les pages Keycloak à l'utilisateur.
     *
     * @param keycloakId      identifiant Keycloak (claim "sub" du JWT)
     * @param username        login Keycloak (claim "preferred_username")
     * @param currentPassword mot de passe actuel, à valider
     * @param newPassword     nouveau mot de passe
     */
    public void changePassword(String keycloakId, String username,
                               String currentPassword, String newPassword) {
        if (username == null || keycloakId == null) {
            throw new IllegalArgumentException("Session invalide : impossible d'identifier l'utilisateur.");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'actuel.");
        }
        if (keycloakAdminService.hasOtpConfigured(username)) {
            throw new IllegalArgumentException(
                    "Votre compte utilise la double authentification. "
                    + "Modifiez votre mot de passe depuis « Gérer mon compte ».");
        }
        if (!keycloakAdminService.verifyPassword(username, currentPassword)) {
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect.");
        }
        keycloakAdminService.resetPassword(keycloakId, newPassword);
        log.info("Mot de passe modifié avec succès pour l'utilisateur {}", username);
    }

    @Transactional
    public void updateUserProfils(Long userId, List<String> profilCodes) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        log.info("Mise à jour des profils pour {} : {}", user.getLogin(), profilCodes);

        profilUtilisateurRepository.deleteByUtilisateurId(user.getId());

        for (String code : profilCodes) {
            profilRepository.findByCode(code).ifPresent(profil -> {
                assignProfilToUser(user, profil);
                try {
                    keycloakAdminService.assignRoleToUser(user.getKeycloack(), profil.getCode());
                } catch (Exception e) {
                    log.error("Erreur assignation rôle {} dans Keycloak : {}", code, e.getMessage());
                }
            });
        }
    }

    private void assignProfilToUser(Utilisateur user, Profil profil) {
        ProfilUtilisateur pu = new ProfilUtilisateur();
        pu.setUtilisateurId(user.getId());
        pu.setProfilId(profil.getId());
        pu.setCode(user.getLogin() + "_" + profil.getCode() + "_" + System.currentTimeMillis());
        profilUtilisateurRepository.save(pu);
        log.info("Profil {} attribué à {}", profil.getCode(), user.getLogin());
    }

    public List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }
}
