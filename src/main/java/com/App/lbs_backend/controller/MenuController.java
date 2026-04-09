package com.App.lbs_backend.controller;

import com.App.lbs_backend.dto.response.MenuResponse;
import com.App.lbs_backend.mapper.MenuMapper;
import com.App.lbs_backend.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final MenuMapper menuMapper;

    /**
     * Endpoint retournant les menus de l'utilisateur connecté
     */
    @GetMapping("/my-menu")
    public ResponseEntity<List<MenuResponse>> getMyMenu() {
        return ResponseEntity.ok(
                menuService.getMyMenus().stream()
                        .map(menuMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }
}
