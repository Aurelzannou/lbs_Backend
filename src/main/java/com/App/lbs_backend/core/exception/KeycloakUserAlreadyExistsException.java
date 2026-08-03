package com.App.lbs_backend.core.exception;

import lombok.Getter;

/** Levée quand Keycloak refuse la création d'un compte car un utilisateur existe déjà avec le
    même login/email (ex: la même adresse est déjà utilisée par un Tuteur). Le mot de passe fourni
    n'a alors PAS été appliqué — l'appelant doit récupérer l'ID du compte existant séparément
    (voir KeycloakAdminService.findUserIdByUsername) plutôt que de considérer que la création a eu lieu. */
@Getter
public class KeycloakUserAlreadyExistsException extends RuntimeException {
    private final String username;

    public KeycloakUserAlreadyExistsException(String username) {
        super("Un compte Keycloak existe déjà avec le login/email : " + username);
        this.username = username;
    }
}
