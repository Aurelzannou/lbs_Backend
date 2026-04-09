package com.App.lbs_backend.controller;

import com.App.lbs_backend.dto.request.ProfilRequest;
import com.App.lbs_backend.dto.response.ProfilResponse;
import com.App.lbs_backend.mapper.ProfilMapper;
import com.App.lbs_backend.service.ProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/administration/profils")
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilService profilService;
    private final ProfilMapper profilMapper;

    @GetMapping
    public ResponseEntity<List<ProfilResponse>> getAll() {
        return ResponseEntity.ok(
                profilService.getAll().stream()
                        .map(profilMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfilResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(profilMapper.toResponse(profilService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ProfilResponse> create(@Valid @RequestBody ProfilRequest request) {
        return ResponseEntity.ok(profilMapper.toResponse(profilService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfilResponse> update(@PathVariable Long id, @Valid @RequestBody ProfilRequest request) {
        return ResponseEntity.ok(profilMapper.toResponse(profilService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profilService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
