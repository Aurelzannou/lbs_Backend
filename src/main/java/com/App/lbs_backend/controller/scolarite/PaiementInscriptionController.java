package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.PaiementInitRequest;
import com.App.lbs_backend.service.paiement.PaiementInscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Paiement en ligne des frais d'inscription (portail parent) via FedaPay.
 * Endpoints authentifiés : seul le parent connecté peut lancer / vérifier son paiement.
 */
@RestController
@RequestMapping("/api/inscription")
@RequiredArgsConstructor
public class PaiementInscriptionController {

    private final PaiementInscriptionService service;
    private final HttpServletRequest httpRequest;

    /** Crée la transaction FedaPay et renvoie de quoi ouvrir le widget inline. */
    @PostMapping("/{dossierId}/paiement/init")
    public ResponseEntity<?> init(@PathVariable Long dossierId,
                                  @RequestBody(required = false) PaiementInitRequest body) {
        String tel = body != null ? body.getTelephonePaiement() : null;
        var res = service.init(dossierId, tel);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Paiement initialisé", res, httpRequest.getRequestURI()));
    }

    /** Relit le statut de la transaction chez FedaPay et met à jour le paiement LBS. */
    @GetMapping("/paiement/{fedapayTransactionId}/verifier")
    public ResponseEntity<?> verifier(@PathVariable String fedapayTransactionId) {
        var res = service.verifier(fedapayTransactionId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Statut du paiement", res, httpRequest.getRequestURI()));
    }
}
