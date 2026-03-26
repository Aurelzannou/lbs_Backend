package com.App.lbs_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
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
}
