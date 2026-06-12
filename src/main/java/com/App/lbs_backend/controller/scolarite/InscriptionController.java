package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.dto.request.SoumettreInscriptionRequest;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.service.scolarite.InscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inscription")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @PostMapping("/soumettre")
    public ResponseEntity<DossierEleveResponse> soumettre(@RequestBody SoumettreInscriptionRequest request) {
        return ResponseEntity.ok(inscriptionService.soumettre(request));
    }
}
