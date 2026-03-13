package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.MenuResponse;
import com.App.lbs_backend.entity.Menu;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuMapper implements Mapper<Menu, MenuResponse> {

    @Override
    public MenuResponse toResponse(Menu entity) {
        if (entity == null) return null;

        List<MenuResponse> enfants = null;
        if (entity.getListeMenuEnfant() != null && !entity.getListeMenuEnfant().isEmpty()) {
            enfants = entity.getListeMenuEnfant()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return new MenuResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getDescription(),
                entity.getPath(),
                entity.getOrdre(),
                entity.getTitre(),
                entity.getMenuEnfantId(),
                enfants
        );
    }
}