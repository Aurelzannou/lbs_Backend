package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ProfesseurResponse(
        Long id,
        String uuid,
        String code,
        String nom,
        String prenom,
        String email,
        String residence,
        String num,
        Boolean actif,
        String keycloakId,
        LocalDateTime modifierLe,
        String modifierPar,
        List<Long> matiereIds,
        List<String> matiereLibelles,
        // Renseigné uniquement dans la réponse immédiate d'une création/mise à jour ayant généré
        // un compte de connexion — jamais persisté, ne peut pas être récupéré ultérieurement.
        String motDePasseGenere
) {}