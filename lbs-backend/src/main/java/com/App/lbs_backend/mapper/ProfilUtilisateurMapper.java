package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ProfilUtilisateurResponse;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import org.springframework.stereotype.Component;

@Component
public class ProfilUtilisateurMapper implements Mapper<ProfilUtilisateur, ProfilUtilisateurResponse> {

    private final ProfilMapper profilMapper;
    private final UtilisateurMapper utilisateurMapper;

    public ProfilUtilisateurMapper(ProfilMapper profilMapper,
                                   UtilisateurMapper utilisateurMapper) {
        this.profilMapper = profilMapper;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    public ProfilUtilisateurResponse toResponse(ProfilUtilisateur entity) {
        if (entity == null) return null;
        return new ProfilUtilisateurResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getProfilId(),
                entity.getUtilisateurId(),
                entity.getToken(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                profilMapper.toResponse(entity.getProfil()),
                utilisateurMapper.toResponse(entity.getUtilisateur())
        );
    }
}