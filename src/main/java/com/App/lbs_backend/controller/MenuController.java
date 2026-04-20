package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.MenuRequest;
import com.App.lbs_backend.dto.response.MenuResponse;
import com.App.lbs_backend.entity.Menu;
import com.App.lbs_backend.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController extends MasterController<Menu, MenuResponse, MenuRequest> {

    private final MenuService menuService;

    @Override
    protected AbstractBaseService<Menu, MenuResponse> service() {
        return menuService;
    }

    /**
     * Endpoint retournant les menus de l'utilisateur connecté
     */
    @GetMapping("/my-menu")
    public ResponseEntity<?> getMyMenu() {
        return sendResponse(
                menuService.getMyMenus().stream()
                        .map(menuService.mapper()::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @Override
    protected MenuResponse doCreate(MenuRequest form) {
        return menuService.mapper().toResponse(menuService.create(form));
    }

    @Override
    protected MenuResponse doUpdate(String uuid, MenuRequest form) {
        Menu menu = menuService.findByUuid(uuid);
        return menuService.mapper().toResponse(menuService.update(menu.getId(), form));
    }
}
