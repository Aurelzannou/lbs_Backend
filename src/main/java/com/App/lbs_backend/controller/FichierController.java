package com.App.lbs_backend.controller;

import com.App.lbs_backend.dto.response.FileResponse;
import com.App.lbs_backend.entity.Fichier;
import com.App.lbs_backend.service.MinioStorageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FichierController {

    private final MinioStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam(value = "folder", defaultValue = "") String folder,
            @RequestParam("file") MultipartFile file) {
        try {
            Fichier uploadedFichier = storageService.uploadFile(folder, file);
            return ResponseEntity.ok(Map.of(
                    "message", "Fichier téléversé avec succès",
                    "file", uploadedFichier,
                    "download", "/api/files/download/" + uploadedFichier.getNom()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/**")
    public ResponseEntity<?> downloadFile(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) {
        try {
            String path = (String) request.getAttribute(org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String bestMatchPattern = (String) request.getAttribute(org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String fileName = new org.springframework.util.AntPathMatcher().extractPathWithinPattern(bestMatchPattern, path);

            FileResponse fileResponse = storageService.downloadFile(fileName);
            MinioStorageService.setHeaderFileSize(response, fileResponse.length());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileResponse.fileName() + "\"")
                    .contentType(MediaType.parseMediaType(fileResponse.type()))
                    .body(new InputStreamResource(fileResponse.stream()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        try {
            storageService.deleteFile(fileName);
            return ResponseEntity.ok(Map.of("message", "Fichier supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
