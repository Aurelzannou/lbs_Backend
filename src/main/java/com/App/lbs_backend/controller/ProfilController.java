package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.ProfilRequest;
import com.App.lbs_backend.dto.response.ProfilResponse;
import com.App.lbs_backend.mapper.ProfilMapper;
import com.App.lbs_backend.service.ProfilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.App.lbs_backend.core.http.response.ApiResponse.apiSuccess;

@RestController
@RequestMapping("/api/administration/profils")
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilService profilService;
    private final ProfilMapper profilMapper;
    private final HttpServletRequest request;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProfilResponse>>> getAll() {
        var data = profilService.getAll().stream()
                        .map(profilMapper::toResponse)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(apiSuccess("Profils récupérés", data, request.getRequestURI()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfilResponse>> getById(@PathVariable Long id) {
        var data = profilMapper.toResponse(profilService.getById(id));
        return ResponseEntity.ok(apiSuccess("Profil récupéré", data, request.getRequestURI()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProfilResponse>> create(@Valid @RequestBody ProfilRequest profilRequest) {
        var data = profilMapper.toResponse(profilService.create(profilRequest));
        return ResponseEntity.ok(apiSuccess("Profil créé", data, request.getRequestURI()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfilResponse>> update(@PathVariable Long id, @Valid @RequestBody ProfilRequest profilRequest) {
        var data = profilMapper.toResponse(profilService.update(id, profilRequest));
        return ResponseEntity.ok(apiSuccess("Profil mis à jour", data, request.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        profilService.delete(id);
        return ResponseEntity.ok(apiSuccess("Profil supprimé", null, request.getRequestURI()));
    }
}
