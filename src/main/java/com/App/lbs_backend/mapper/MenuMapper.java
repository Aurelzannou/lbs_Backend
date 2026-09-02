package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.request.MenuRequest;
import com.App.lbs_backend.dto.response.MenuResponse;
import com.App.lbs_backend.entity.Menu;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        return toResponse(entity, enfants);
    }

    /** Variante utilisée pour construire l'arbre des menus autorisés : les enfants sont fournis
        (déjà filtrés par profil) au lieu d'être dérivés récursivement de l'entité. */
    public MenuResponse toResponse(Menu entity, List<MenuResponse> enfants) {
        if (entity == null) return null;

        List<Long> profilIds = entity.getListeProfilMenu() == null ? List.of() :
                entity.getListeProfilMenu().stream()
                        // profilId (colonne en lecture seule) peut ne pas être encore hydraté juste
                        // après un save : on retombe alors sur la relation profil.
                        .map(pm -> pm.getProfilId() != null ? pm.getProfilId()
                                : (pm.getProfil() != null ? pm.getProfil().getId() : null))
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

        return new MenuResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getDescription(),
                entity.getIcon(),
                entity.getPath(),
                entity.getOrdre(),
                entity.getTitre(),
                entity.getMenuEnfantId(),
                enfants,
                profilIds
        );
    }
    public void updateEntity(Menu entity, MenuRequest request) {
        if (request == null) return;
        entity.setCode(request.getCode());
        entity.setTitre(request.getTitre());
        entity.setDescription(request.getDescription());
        entity.setPath(request.getPath());
        entity.setOrdre(request.getOrdre());
        entity.setMenuEnfantId(request.getMenuEnfantId());
    }
}