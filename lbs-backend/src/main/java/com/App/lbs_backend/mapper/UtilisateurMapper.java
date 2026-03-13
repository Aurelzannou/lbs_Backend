package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper implements Mapper<Utilisateur, UtilisateurResponse> {

    @Override
    public UtilisateurResponse toResponse(Utilisateur entity) {
        if (entity == null) return null;
        return new UtilisateurResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getLogin(),
                entity.getPhoto(),
                entity.getSexe(),
                entity.getKeycloack()
        );
    }
}