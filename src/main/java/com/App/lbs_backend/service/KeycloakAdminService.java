package com.App.lbs_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

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
            } else {
                String error = response.readEntity(String.class);
                log.error("Erreur lors de la création de l'utilisateur Keycloak. Status: {}, Erreur: {}", 
                          response.getStatus(), error);
                throw new RuntimeException("Erreur de création d'utilisateur dans Keycloak: " + error);
            }
        } catch (Exception e) {
            log.error("Exception lors de la création de l'utilisateur dans Keycloak: ", e);
            throw new RuntimeException("Erreur de synchronisation Keycloak", e);
        }
    }
}
