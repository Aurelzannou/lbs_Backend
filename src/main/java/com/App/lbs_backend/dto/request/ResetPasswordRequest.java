package com.App.lbs_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Consomme un lien « Mot de passe oublié » : l'utilisateur choisit lui-même son nouveau
 * mot de passe. Le jeton reçu par email prouve qu'il contrôle la boîte mail du compte.
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Le lien de réinitialisation est incomplet.")
    private String token;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    private String newPassword;
}
