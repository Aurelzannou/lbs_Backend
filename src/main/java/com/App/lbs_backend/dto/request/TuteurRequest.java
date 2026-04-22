package com.App.lbs_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TuteurRequest {
    private String code;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone1;
    
    private String telephone2;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    private String profession;
    private String adresse;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}
