package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.dto.auth.AuthResponse;
import com.App.lbs_backend.dto.auth.LoginRequest;
import com.App.lbs_backend.dto.request.TuteurRequest;
import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.service.scolarite.TuteurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portail/auth")
@RequiredArgsConstructor
public class TuteurAuthController {

    private final TuteurService tuteurService;

    @PostMapping("/register")
    public ResponseEntity<TuteurResponse> register(@Valid @RequestBody TuteurRequest request) {
        return ResponseEntity.ok(tuteurService.register(request));
    }
}
