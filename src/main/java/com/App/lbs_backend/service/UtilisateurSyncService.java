package com.App.lbs_backend.service;

import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import com.App.lbs_backend.repository.ProfilRepository;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import com.App.lbs_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service gérant la synchronisation entre l'utilisateur connecté via JWT 
 * et la base de données locale lbs_utilisateur.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurSyncService {

    private final UtilisateurRepository utilisateurRepository;
    private final ProfilRepository profilRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    /**
     * Récupère l'utilisateur actuellement connecté depuis le contexte de sécurité.
     * Si c'est sa première connexion, il est créé automatiquement en base locale.
     */
    @Transactional
    public Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new RuntimeException("Accès non autorisé : Contexte de sécurité invalide");
        }
        
        Jwt jwt = jwtAuth.getToken();
        String keycloakId = jwt.getSubject(); // "sub" claim dans le JWT
        
        return utilisateurRepository.findByKeycloack(keycloakId)
                .orElseGet(() -> syncNewUserFromJwt(jwt));
    }

    /**
     * Crée un utilisateur local s'il n'existe pas encore
     */
    private Utilisateur syncNewUserFromJwt(Jwt jwt) {
        log.info("Création locale d'un nouvel utilisateur Keycloak : {}", jwt.getSubject());
        
        Utilisateur newUser = new Utilisateur();
        newUser.setKeycloack(jwt.getSubject());
        newUser.setLogin(jwt.getClaimAsString("preferred_username"));
        
        // Keycloak peut envoyer given_name et family_name
        newUser.setPrenom(jwt.getClaimAsString("given_name"));
        newUser.setNom(jwt.getClaimAsString("family_name"));
        
        // Nom complet si pas de split prenom/nom
        if (newUser.getPrenom() == null && newUser.getNom() == null) {
            String name = jwt.getClaimAsString("name");
            if (name != null) {
                newUser.setNom(name);
            } else {
                newUser.setNom(newUser.getLogin());
            }
        }
        
        Utilisateur savedUser = utilisateurRepository.save(newUser);

        // Attribution du profil par défaut (LECTEUR) lors de la première synchro
        profilRepository.findByCode("LECTEUR").ifPresent(profil -> {
            ProfilUtilisateur pu = new ProfilUtilisateur();
            pu.setUtilisateurId(savedUser.getId());
            pu.setProfilId(profil.getId());
            pu.setCode(savedUser.getLogin() + "_LECTEUR");
            profilUtilisateurRepository.save(pu);
            log.info("Profil par défaut LECTEUR attribué à {}", savedUser.getLogin());
        });

        return savedUser;
    }
}
