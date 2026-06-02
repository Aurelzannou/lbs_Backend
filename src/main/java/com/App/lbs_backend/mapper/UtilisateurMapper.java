package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UtilisateurMapper implements Mapper<Utilisateur, UtilisateurResponse> {

    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    @Override
    public UtilisateurResponse toResponse(Utilisateur entity) {
        if (entity == null) return null;

        List<String> profils = profilUtilisateurRepository
                .findByUtilisateurId(entity.getId())
                .stream()
                .map(pu -> pu.getProfil() != null ? pu.getProfil().getCode() : null)
                .filter(code -> code != null)
                .collect(Collectors.toList());

        return new UtilisateurResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getLogin(),
                entity.getEmail(),
                entity.getPhoto(),
                entity.getSexe(),
                entity.getKeycloack(),
                profils
        );
    }
}
