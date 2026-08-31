package com.App.lbs_backend.controller;

import com.App.lbs_backend.service.paiement.PaiementInscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Réception des notifications FedaPay (transaction.approved, transaction.declined, ...).
 * Endpoint PUBLIC (pas de JWT) — l'authenticité est garantie par la signature HMAC.
 * À déclarer dans FedaPay ▸ Webhooks : POST {URL_PUBLIQUE}/api/webhooks/fedapay
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class FedaPayWebhookController {

    private final PaiementInscriptionService service;

    @PostMapping("/fedapay")
    public ResponseEntity<String> fedapay(
            @RequestBody String rawBody,
            @RequestHeader(value = "x-fedapay-signature", required = false) String signature) {
        service.handleWebhook(rawBody, signature);
        return ResponseEntity.ok("ok");
    }
}
