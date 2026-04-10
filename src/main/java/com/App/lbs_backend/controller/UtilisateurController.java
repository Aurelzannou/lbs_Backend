package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.FormRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.service.AuthService;
import com.App.lbs_backend.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.App.lbs_backend.core.http.response.ApiResponse.apiSuccess;

@RestController
@RequestMapping("/api/administration/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController extends MasterController<Utilisateur, UtilisateurResponse, FormRequest> {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;

    @Override
    protected AbstractBaseService<Utilisateur, UtilisateurResponse> service() {
        return utilisateurService;
    }

    @PostMapping("/{id}/profils")
    public ResponseEntity<ApiResponse<Void>> updateProfils(@PathVariable Long id, @RequestBody List<String> profilCodes) {
        authService.updateUserProfils(id, profilCodes);
        return ResponseEntity.ok(apiSuccess("Profils mis à jour avec succès", null, request.getRequestURI()));
    }
}
