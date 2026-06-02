package com.App.lbs_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    // "ADMIN" ou "PARENT" — détermine le type de compte à créer
    private String userType;

    // Requis uniquement pour ADMIN (identifiant de connexion)
    private String username;

    // Profil optionnel pour ADMIN (défaut : LECTEUR)
    private String role;

    // Requis uniquement pour PARENT
    private String telephone;
}
