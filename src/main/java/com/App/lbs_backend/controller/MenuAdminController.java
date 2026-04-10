package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.MenuRequest;
import com.App.lbs_backend.dto.response.MenuResponse;
import com.App.lbs_backend.mapper.MenuMapper;
import com.App.lbs_backend.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.App.lbs_backend.core.http.response.ApiResponse.apiSuccess;

@RestController
@RequestMapping("/api/administration/menus")
@RequiredArgsConstructor
public class MenuAdminController {

    private final MenuService menuService;
    private final MenuMapper menuMapper;
    private final HttpServletRequest request;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAll() {
        var data = menuService.getAll().stream()
                        .map(menuMapper::toResponse)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(apiSuccess("Menus récupérés", data, request.getRequestURI()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> getById(@PathVariable Long id) {
        var data = menuMapper.toResponse(menuService.getById(id));
        return ResponseEntity.ok(apiSuccess("Menu récupéré", data, request.getRequestURI()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuResponse>> create(@Valid @RequestBody MenuRequest requestDTO) {
        var data = menuMapper.toResponse(menuService.create(requestDTO));
        return ResponseEntity.ok(apiSuccess("Menu créé", data, request.getRequestURI()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> update(@PathVariable Long id, @Valid @RequestBody MenuRequest requestDTO) {
        var data = menuMapper.toResponse(menuService.update(id, requestDTO));
        return ResponseEntity.ok(apiSuccess("Menu mis à jour", data, request.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.ok(apiSuccess("Menu supprimé", null, request.getRequestURI()));
    }
}
