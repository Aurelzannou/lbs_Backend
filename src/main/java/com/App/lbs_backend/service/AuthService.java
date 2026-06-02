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

    @Transactional
    public void register(RegisterRequest request) {
        String userType = (request.getUserType() != null) ? request.getUserType().toUpperCase() : "ADMIN";
        if ("PARENT".equals(userType)) {
            registerParent(request);
        } else {
            registerAdmin(request);
        }
    }

    private void registerAdmin(RegisterRequest request) {
        log.info("Inscription administrateur : {}", request.getUsername());

        if (utilisateurRepository.existsByLogin(request.getUsername())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé.");
        }
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée.");
        }

        String keycloakId = keycloakAdminService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword()
        );

        Utilisateur user = new Utilisateur();
        user.setKeycloack(keycloakId);
        user.setLogin(request.getUsername());
        user.setEmail(request.getEmail());
        user.setNom(request.getLastName());
        user.setPrenom(request.getFirstName());
        utilisateurRepository.save(user);

        String roleToAssign = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole() : "LECTEUR";

        profilRepository.findByCode(roleToAssign).ifPresentOrElse(profil -> {
            assignProfilToUser(user, profil);
            try {
                keycloakAdminService.assignRoleToUser(keycloakId, profil.getCode());
            } catch (Exception e) {
                log.error("Erreur assignation rôle {} dans Keycloak : {}", roleToAssign, e.getMessage());
            }
        }, () -> log.warn("Profil '{}' non trouvé.", roleToAssign));
    }

    private void registerParent(RegisterRequest request) {
        log.info("Inscription parent d'élève : {}", request.getEmail());

        if (tuteurRepository.findByEmail(request.getEmail()).isPresent()) {
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

        try {
            keycloakAdminService.assignRoleToUser(keycloakId, "TUTEUR");
        } catch (Exception e) {
            log.error("Erreur assignation rôle TUTEUR dans Keycloak : {}", e.getMessage());
        }

        Tuteur tuteur = new Tuteur();
        tuteur.setKeycloakId(keycloakId);
        tuteur.setNom(request.getLastName());
        tuteur.setPrenom(request.getFirstName());
        tuteur.setEmail(request.getEmail());
        tuteur.setTelephone1(request.getTelephone());
        tuteur.setActif(true);
        tuteurRepository.save(tuteur);

        log.info("Parent d'élève enregistré avec succès : {}", request.getEmail());
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
