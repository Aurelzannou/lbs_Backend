package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ProfilMenuResponse;
import com.App.lbs_backend.entity.ProfilMenu;
import org.springframework.stereotype.Component;

@Component
public class ProfilMenuMapper implements Mapper<ProfilMenu, ProfilMenuResponse> {

    private final ProfilMapper profilMapper;
    private final MenuMapper menuMapper;

    public ProfilMenuMapper(ProfilMapper profilMapper, MenuMapper menuMapper) {
        this.profilMapper = profilMapper;
        this.menuMapper = menuMapper;
    }

    @Override
    public ProfilMenuResponse toResponse(ProfilMenu entity) {
        if (entity == null) return null;
        return new ProfilMenuResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getProfilId(),
                entity.getMenuId(),
                entity.getToken(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                profilMapper.toResponse(entity.getProfil()),
                menuMapper.toResponse(entity.getMenu())
        );
    }
}