package com.App.lbs_backend.service;

import com.App.lbs_backend.dto.request.RegisterRequest;
import com.App.lbs_backend.entity.Profil;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.repository.ProfilRepository;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import com.App.lbs_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KeycloakAdminService keycloakAdminService;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    /**
     * Gère l'inscription d'un nouvel utilisateur :
     * 1. Création dans Keycloak
     * 2. Création en base locale
     * 3. Attribution du profil par défaut (LECTEUR)
     */
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Processus d'inscription pour l'utilisateur : {}", request.getUsername());

        // 1. Création dans Keycloak
        String keycloakId = keycloakAdminService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
        );

        // 2. Création locale
        Utilisateur user = new Utilisateur();
        user.setKeycloack(keycloakId);
        user.setLogin(request.getUsername());
        user.setNom(request.getLastName());
        user.setPrenom(request.getFirstName());
        utilisateurRepository.save(user);

        // 3. Attribution du profil par défaut (LECTEUR)
        profilRepository.findByCode("LECTEUR").ifPresentOrElse(profil -> {
            assignProfilToUser(user, profil);
            // Synchroniser le rôle dans Keycloak (Optionnel mais recommandé)
            keycloakAdminService.assignRoleToUser(keycloakId, profil.getCode());
        }, () -> log.warn("Profil par défaut 'LECTEUR' non trouvé. Aucune attribution effectuée."));
    }

    private void assignProfilToUser(Utilisateur user, Profil profil) {
        ProfilUtilisateur pu = new ProfilUtilisateur();
        pu.setUtilisateurId(user.getId());
        pu.setProfilId(profil.getId());
        pu.setCode(user.getLogin() + "_" + profil.getCode());
        profilUtilisateurRepository.save(pu);
        log.info("Profil {} attribué à l'utilisateur {}", profil.getCode(), user.getLogin());
    }
}
