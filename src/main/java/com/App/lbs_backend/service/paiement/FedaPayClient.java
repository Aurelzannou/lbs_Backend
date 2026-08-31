package com.App.lbs_backend.service.paiement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client HTTP minimal pour l'API FedaPay (agrégateur Mobile Money Bénin).
 *
 * Sandbox : https://sandbox-api.fedapay.com/v1
 * Live    : https://api.fedapay.com/v1
 *
 * Corps de requête = Map (sérialisation JSON fiable, indépendante de la version Jackson).
 * Réponse lue en String puis parsée avec un ObjectMapper local.
 */
@Component
@Slf4j
public class FedaPayClient {

    private final RestClient http = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${fedapay.base-url:https://sandbox-api.fedapay.com/v1}")
    private String baseUrl;

    @Value("${fedapay.secret-key:}")
    private String secretKey;

    /** Représentation du client pour FedaPay (facultatif mais recommandé pour le reçu). */
    public record Customer(String firstname, String lastname, String email,
                           String phoneNumber, String phoneCountry) {}

    /**
     * Crée une transaction FedaPay et renvoie son identifiant.
     *
     * @param description libellé affiché au payeur / sur le reçu
     * @param amount      montant en XOF (entier)
     * @param callbackUrl URL de retour après paiement (peut être null)
     * @param customer    infos du payeur (peut être null)
     * @return l'id numérique de la transaction FedaPay
     */
    public long createTransaction(String description, long amount, String callbackUrl, Customer customer) {
        ensureConfigured();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("description", description);
        body.put("amount", amount);
        body.put("currency", Map.of("iso", "XOF"));
        if (callbackUrl != null && !callbackUrl.isBlank()) {
            body.put("callback_url", callbackUrl);
        }
        if (customer != null) {
            Map<String, Object> c = new LinkedHashMap<>();
            if (customer.firstname() != null) c.put("firstname", customer.firstname());
            if (customer.lastname() != null)  c.put("lastname", customer.lastname());
            if (customer.email() != null)     c.put("email", customer.email());
            if (customer.phoneNumber() != null && !customer.phoneNumber().isBlank()) {
                c.put("phone_number", Map.of(
                        "number", customer.phoneNumber(),
                        "country", customer.phoneCountry() != null ? customer.phoneCountry() : "bj"));
            }
            if (!c.isEmpty()) body.put("customer", c);
        }

        if (log.isInfoEnabled()) {
            try { log.info("[fedapay] POST /transactions body={}", mapper.writeValueAsString(body)); }
            catch (Exception ignored) { }
        }
        JsonNode res = post("/transactions", body);
        JsonNode tx = res.has("v1/transaction") ? res.get("v1/transaction") : res;
        long id = tx.path("id").asLong(0);
        if (id == 0) {
            throw new IllegalArgumentException("FedaPay : réponse inattendue à la création de transaction.");
        }
        log.info("[fedapay] transaction créée id={} montant={} XOF", id, amount);
        return id;
    }

    /**
     * Génère un jeton de paiement : renvoie l'URL de la page de paiement hébergée FedaPay
     * (repli si le widget inline ne peut pas s'ouvrir). Null si indisponible.
     */
    public String createPaymentUrl(long transactionId) {
        ensureConfigured();
        JsonNode res = post("/transactions/" + transactionId + "/token", Map.of());
        String url = res.path("url").asText(null);
        if (url == null) url = res.path("v1/token").path("url").asText(null);
        if (url == null) url = res.path("token").asText(null);
        return url;
    }

    /**
     * Statut d'une transaction : pending | approved | declined | canceled | refunded | transferred
     */
    public String getTransactionStatus(long transactionId) {
        ensureConfigured();
        JsonNode res = get("/transactions/" + transactionId);
        JsonNode tx = res.has("v1/transaction") ? res.get("v1/transaction") : res;
        return tx.path("status").asText("unknown");
    }

    // ─────────────────────────────────────────────────────────────
    private void ensureConfigured() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException(
                    "FedaPay n'est pas configuré : ajoute la variable d'environnement FEDAPAY_SECRET_KEY.");
        }
    }

    private JsonNode post(String path, Object body) {
        try {
            String raw = http.post()
                    .uri(baseUrl + path)
                    .header("Authorization", "Bearer " + secretKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parse(raw);
        } catch (RestClientResponseException e) {
            log.error("[fedapay] POST {} → HTTP {} : {}", path, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new IllegalArgumentException(messageErreur(e));
        }
    }

    private JsonNode get(String path) {
        try {
            String raw = http.get()
                    .uri(baseUrl + path)
                    .header("Authorization", "Bearer " + secretKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
            return parse(raw);
        } catch (RestClientResponseException e) {
            log.error("[fedapay] GET {} → HTTP {} : {}", path, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new IllegalArgumentException(messageErreur(e));
        }
    }

    private JsonNode parse(String raw) {
        try {
            return mapper.readTree(raw == null ? "{}" : raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("FedaPay : réponse illisible.");
        }
    }

    /** Extrait le message d'erreur renvoyé par FedaPay, sinon un message générique. */
    private String messageErreur(RestClientResponseException e) {
        try {
            JsonNode n = mapper.readTree(e.getResponseBodyAsString());
            String m = n.path("message").asText(null);
            if (m != null && !m.isBlank()) return "FedaPay : " + m;
        } catch (Exception ignored) { }
        return "Le service de paiement a refusé la demande (HTTP " + e.getStatusCode().value() + ").";
    }
}
