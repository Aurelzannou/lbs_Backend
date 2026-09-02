package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.CaisseResponse;
import com.App.lbs_backend.entity.Caisse;
import com.App.lbs_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaisseMapper implements Mapper<Caisse, CaisseResponse> {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public CaisseResponse toResponse(Caisse entity) {
        if (entity == null) return null;

        String nomComplet = entity.getUtilisateurId() == null ? null
                : utilisateurRepository.findById(entity.getUtilisateurId())
                    .map(u -> ((u.getPrenom() != null ? u.getPrenom() + " " : "") + (u.getNom() != null ? u.getNom() : "")).trim())
                    .filter(s -> !s.isBlank())
                    .orElse(null);

        return new CaisseResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getSolde(),
                entity.getActif(),
                entity.getUtilisateurId(),
                nomComplet,
                entity.getModifierLe(),
                entity.getModifierPar()
        );
    }
}
