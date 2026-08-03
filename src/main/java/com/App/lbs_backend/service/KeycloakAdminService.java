package com.App.lbs_backend.service;

import com.App.lbs_backend.core.exception.KeycloakUserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdminService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.target-realm:lbs-realm}")
    private String targetRealm;

    /**
     * Crée un rôle dynamiquement dans Keycloak
     *
     * @param roleName Le nom du rôle (ex: "DIRECTEUR")
     * @param description Une description facultative
     */
    public void createRole(String roleName, String description) {
        log.info("Tentative de création du rôle Keycloak : {}", roleName);
        try {
            RolesResource rolesResource = keycloak.realm(targetRealm).roles();
            
            // Vérifier si le rôle existe déjà
            boolean exists = rolesResource.list().stream()
                    .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));
            
            if (!exists) {
                RoleRepresentation role = new RoleRepresentation();
                role.setName(roleName);
                role.setDescription(description);
                rolesResource.create(role);
                log.info("Création réussie du rôle : {}", roleName);
            } else {
                log.warn("Le rôle {} existe déjà dans Keycloak.", roleName);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la création du rôle dans Keycloak: ", e);
            throw new RuntimeException("Erreur de synchronisation Keycloak", e);
        }
    }

    /**
     * Assigne un rôle à un utilisateur dans Keycloak.
     * Cette méthode sera utilisée lors de l'assignation d'un Profil.
     * 
     * @param keycloakUserId L'ID unique Keycloak de l'utilisateur (UUID)
     * @param roleName Le nom du rôle à assigner
     */
    public void assignRoleToUser(String keycloakUserId, String roleName) {
        log.info("Assignation du rôle {} à l'utilisateur ID: {}", roleName, keycloakUserId);
        try {
            UsersResource usersResource = keycloak.realm(targetRealm).users();
            RoleRepresentation realmRole = keycloak.realm(targetRealm).roles().get(roleName).toRepresentation();
            
            usersResource.get(keycloakUserId).roles().realmLevel()
                         .add(Collections.singletonList(realmRole));
                         
            log.info("Assignation réussie.");
        } catch (Exception e) {
            log.error("Erreur lors de l'assignation du rôle dans Keycloak: ", e);
        }
    }

    /**
     * Crée un utilisateur dans Keycloak.
     *
     * @param username Le nom d'utilisateur
     * @param email L'adresse email
     * @param firstName Le prénom
     * @param lastName Le nom
     * @param password Le mot de passe
     * @return L'ID unique Keycloak de l'utilisateur créé
     */
    public String createUser(String username, String email, String firstName, String lastName, String password) {
        log.info("Création d'un nouvel utilisateur Keycloak : {}", username);
        
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Configuration du mot de passe
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        try {
            UsersResource usersResource = keycloak.realm(targetRealm).users();
            Response response = usersResource.create(user);

            if (response.getStatus() == 201) {
                // Récupérer l'ID de l'utilisateur créé à partir du header Location
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                log.info("Utilisateur créé avec succès. ID: {}", userId);
                return userId;
            }

            if (response.getStatus() == 409) {
                // Un compte existe déjà avec ce login/email (ex: la même adresse sert déjà à un
                // Tuteur) — le mot de passe demandé n'a PAS été appliqué à ce compte existant.
                log.warn("Un compte Keycloak existe déjà pour {} — le mot de passe fourni n'a pas été appliqué.", username);
                throw new KeycloakUserAlreadyExistsException(username);
            }

            String error = response.readEntity(String.class);
            log.error("Erreur lors de la création de l'utilisateur Keycloak. Status: {}, Erreur: {}",
                      response.getStatus(), error);
            throw new RuntimeException("Erreur de création d'utilisateur dans Keycloak: " + error);
        } catch (KeycloakUserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception lors de la création de l'utilisateur dans Keycloak: ", e);
            throw new RuntimeException("Erreur de synchronisation Keycloak", e);
        }
    }

    /** Retrouve l'ID Keycloak d'un utilisateur existant par son login (username = email dans ce
        système, cf. hasOtpConfigured) — utilisé quand createUser signale qu'un compte existe déjà. */
    public String findUserIdByUsername(String username) {
        List<UserRepresentation> matches = keycloak.realm(targetRealm).users().search(username, true);
        return matches.isEmpty() ? null : matches.get(0).getId();
    }

    /**
     * Active ou désactive un compte Keycloak existant (ex: un professeur qui devient inactif
     * dans le référentiel ne doit plus pouvoir se connecter à son portail).
     *
     * @param keycloakUserId L'ID unique Keycloak de l'utilisateur
     * @param enabled true pour activer le compte, false pour le désactiver
     */
    public void setUserEnabled(String keycloakUserId, boolean enabled) {
        log.info("{} le compte Keycloak ID: {}", enabled ? "Activation" : "Désactivation", keycloakUserId);
        try {
            UserResource userResource = keycloak.realm(targetRealm).users().get(keycloakUserId);
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(enabled);
            userResource.update(user);
            log.info("Compte Keycloak {} avec succès.", enabled ? "activé" : "désactivé");
        } catch (Exception e) {
            log.error("Erreur lors de l'activation/désactivation du compte Keycloak : ", e);
        }
    }

    /**
     * Réinitialise le mot de passe d'un compte Keycloak existant (ex: l'admin veut renvoyer de
     * nouveaux identifiants à un professeur qui a perdu les siens).
     *
     * @param keycloakUserId L'ID unique Keycloak de l'utilisateur
     * @param newPassword Le nouveau mot de passe à appliquer
     */
    public void resetPassword(String keycloakUserId, String newPassword) {
        log.info("Réinitialisation du mot de passe pour le compte Keycloak ID: {}", keycloakUserId);
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);
            keycloak.realm(targetRealm).users().get(keycloakUserId).resetPassword(credential);
            log.info("Mot de passe réinitialisé avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de la réinitialisation du mot de passe Keycloak : ", e);
            throw new RuntimeException("Erreur de synchronisation Keycloak", e);
        }
    }

    /**
     * Indique si l'utilisateur (identifié par son login Keycloak — username ou email selon le
     * type de compte) a déjà configuré un second facteur OTP (TOTP). Utilisé pour désambiguïser
     * côté connexion entre "mot de passe invalide" et "code OTP requis".
     *
     * @param username Le login Keycloak de l'utilisateur (username pour un admin, email pour un tuteur)
     * @return true si un credential de type OTP est configuré, false si l'utilisateur n'existe pas
     *         ou n'a pas encore activé l'OTP.
     */
    public boolean hasOtpConfigured(String username) {
        try {
            UsersResource usersResource = keycloak.realm(targetRealm).users();
            List<UserRepresentation> matches = usersResource.search(username, true);
            if (matches.isEmpty()) {
                return false;
            }
            String userId = matches.get(0).getId();
            List<CredentialRepresentation> credentials = usersResource.get(userId).credentials();
            return credentials.stream().anyMatch(c -> "otp".equals(c.getType()));
        } catch (Exception e) {
            log.error("Erreur lors de la vérification OTP pour {} : {}", username, e.getMessage());
            return false;
        }
    }
}
