package com.App.lbs_backend.service.paiement;

import com.App.lbs_backend.dto.response.PaiementInitResponse;
import com.App.lbs_backend.dto.response.PaiementStatutResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.entity.TypeFrais;
import com.App.lbs_backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;

/**
 * Paiement en ligne des FRAIS D'INSCRIPTION via FedaPay (widget inline).
 *
 *  1. init()      : crée la transaction FedaPay + le Paiement (statut INITIE), renvoie
 *                   l'id de transaction + la clé publique au navigateur.
 *  2. verifier()  : le front appelle après le widget → on relit le statut chez FedaPay
 *                   (source autoritative) et on met à jour le Paiement.
 *  3. handleWebhook() : FedaPay notifie le backend directement (fiable même si le parent
 *                   ferme l'onglet).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementInscriptionService {

    /** Code du type de frais encaissé à l'inscription (référentiel « Types de frais »). */
    @Value("${fedapay.type-frais-inscription:INSCRIPTION}")
    private String typeFraisInscriptionCode;

    private final FedaPayClient            fedaPayClient;
    private final DossierEleveRepository   dossierEleveRepository;
    private final FraisScolaireRepository  fraisScolaireRepository;
    private final TypeFraisRepository      typeFraisRepository;
    private final PaiementRepository       paiementRepository;
    private final TuteurRepository         tuteurRepository;

    private final ObjectMapper json = new ObjectMapper();

    @Value("${fedapay.public-key:}")
    private String publicKey;

    @Value("${fedapay.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // ─────────────────────────────────────────────────────────────
    //  1. Initialisation
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public PaiementInitResponse init(Long dossierId, String telephonePaiement) {
        DossierEleve dossier = dossierEleveRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier d'inscription introuvable."));
        FraisScolaire frais = fraisInscription(dossier.getClasseId(), dossier.getAnneeScolaireId());
        return initTransaction(dossier, frais, null, telephonePaiement, false);
    }

    /**
     * Paiement en ligne d'un frais scolaire quelconque depuis le portail parent (scolarité,
     * cantine…). Le parent choisit le montant (une tranche, un montant libre, ou le reste) :
     * {@code montantChoisi} doit être compris entre 1 et le reste à payer. {@code null} = solde
     * du restant dû.
     */
    @Transactional
    public PaiementInitResponse initPourFrais(Long dossierId, Long fraisScolaireId,
                                              Long montantChoisi, String telephonePaiement) {
        DossierEleve dossier = dossierEleveRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));
        FraisScolaire frais = fraisScolaireRepository.findById(fraisScolaireId)
                .orElseThrow(() -> new IllegalArgumentException("Frais scolaire introuvable."));

        if (!java.util.Objects.equals(frais.getClasseId(), dossier.getClasseId())
                || !java.util.Objects.equals(frais.getAnneeScolaireId(), dossier.getAnneeScolaireId())) {
            throw new IllegalArgumentException("Ce frais ne correspond pas à la classe / année de ce dossier.");
        }
        // Les frais d'inscription se règlent pendant le parcours d'inscription, pas ici.
        typeFraisRepository.findByCode(typeFraisInscriptionCode)
                .map(t -> ((TypeFrais) t).getId())
                .filter(id -> id.equals(frais.getTypeFraisId()))
                .ifPresent(id -> {
                    throw new IllegalArgumentException(
                            "Les frais d'inscription se paient lors de l'inscription, pas depuis cet écran.");
                });
        return initTransaction(dossier, frais, montantChoisi, telephonePaiement, true);
    }

    // ─────────────────────────────────────────────────────────────
    private PaiementInitResponse initTransaction(DossierEleve dossier, FraisScolaire frais,
                                                 Long montantChoisi, String telephonePaiement,
                                                 boolean paiementPartielAutorise) {
        long montantFrais = frais.getMontant() != null ? Math.round(frais.getMontant()) : 0;
        if (montantFrais <= 0) {
            throw new IllegalArgumentException("Le montant de ce frais doit être supérieur à 0.");
        }

        double dejaPaye = paiementRepository
                .findByDossierEleveIdAndFraisScolaireIdAndStatutTransaction(dossier.getId(), frais.getId(), "SUCCES")
                .stream().mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0).sum();

        long montant;
        if (paiementPartielAutorise) {
            long reste = Math.round(montantFrais - dejaPaye);
            if (reste <= 0) {
                throw new IllegalArgumentException("Ce frais est déjà entièrement réglé.");
            }
            if (montantChoisi != null) {
                if (montantChoisi < 1) {
                    throw new IllegalArgumentException("Le montant à payer doit être d'au moins 1 FCFA.");
                }
                if (montantChoisi > reste) {
                    throw new IllegalArgumentException(
                            "Le montant saisi dépasse le reste à payer (" + reste + " FCFA).");
                }
                montant = montantChoisi;
            } else {
                montant = reste; // par défaut : solde du restant dû
            }
        } else {
            if (dejaPaye > 0) {
                throw new IllegalArgumentException("Les frais d'inscription de ce dossier sont déjà réglés.");
            }
            montant = montantFrais;
        }

        String typeLibelle = typeFraisRepository.findById(frais.getTypeFraisId())
                .map(t -> ((TypeFrais) t).getLibelle()).orElse("Frais scolaire");

        // Client (pour le reçu FedaPay)
        final String tel = telephonePaiement;
        FedaPayClient.Customer customer = tuteurRepository.findById(dossier.getTuteurId() == null ? -1L : dossier.getTuteurId())
                .map(t -> new FedaPayClient.Customer(
                        t.getPrenom(), t.getNom(), t.getEmail(),
                        cleanPhone(tel != null ? tel : t.getTelephone1()), "bj"))
                .orElse(new FedaPayClient.Customer(
                        dossier.getPrenom(), dossier.getNom(), null, cleanPhone(tel), "bj"));

        String description = typeLibelle + " - " + dossier.getNom() + " " + dossier.getPrenom()
                + " (dossier " + dossier.getNumero() + ")";
        String callback = frontendUrl + "/portail/dashboard";

        long txId = fedaPayClient.createTransaction(description, montant, callback, customer);
        String checkoutUrl = null;
        try {
            checkoutUrl = fedaPayClient.createPaymentUrl(txId);
        } catch (Exception e) {
            log.warn("[paiement-inscription] URL de repli FedaPay indisponible : {}", e.getMessage());
        }

        // Réutilise un Paiement INITIE existant pour ce dossier/frais, sinon en crée un
        Paiement paiement = paiementRepository
                .findByDossierEleveIdAndFraisScolaireId(dossier.getId(), frais.getId())
                .stream().filter(p -> !"SUCCES".equals(p.getStatutTransaction()))
                .findFirst()
                .orElseGet(Paiement::new);

        paiement.setDossierEleveId(dossier.getId());
        paiement.setFraisScolaireId(frais.getId());
        paiement.setMontant((double) montant);
        paiement.setReference(String.valueOf(txId));
        paiement.setCanal("EN_LIGNE");
        paiement.setStatutTransaction("INITIE");
        paiement.setTelephonePaiement(cleanPhone(tel));
        paiementRepository.save(paiement);

        log.info("[paiement-en-ligne] dossier={} frais={} tx={} montant={} XOF",
                dossier.getId(), frais.getId(), txId, montant);
        return new PaiementInitResponse(txId, publicKey, montant, "XOF", description, checkoutUrl);
    }

    // ─────────────────────────────────────────────────────────────
    //  2. Vérification (appelée par le front après le widget)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public PaiementStatutResponse verifier(String fedapayTransactionId) {
        Paiement paiement = paiementRepository.findByReference(fedapayTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable pour cette transaction."));

        String fedapayStatut = fedaPayClient.getTransactionStatus(Long.parseLong(fedapayTransactionId));
        appliquerStatut(paiement, fedapayStatut);

        boolean paye = "SUCCES".equals(paiement.getStatutTransaction());
        return new PaiementStatutResponse(paiement.getStatutTransaction(), fedapayStatut, paye);
    }

    // ─────────────────────────────────────────────────────────────
    //  3. Webhook FedaPay
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String rawBody, String signatureHeader) {
        verifierSignature(rawBody, signatureHeader);
        try {
            JsonNode root = json.readTree(rawBody);
            JsonNode entity = root.path("entity");
            if (entity.isMissingNode()) entity = root.path("data").path("object"); // tolérance
            String txId = entity.path("id").asText(null);
            String statut = entity.path("status").asText(null);
            if (txId == null || statut == null) {
                log.warn("[fedapay-webhook] payload sans id/status : {}", rawBody);
                return;
            }
            paiementRepository.findByReference(txId).ifPresentOrElse(
                    p -> {
                        appliquerStatut(p, statut);
                        log.info("[fedapay-webhook] tx={} statut FedaPay={} → paiement {}", txId, statut, p.getStatutTransaction());
                    },
                    () -> log.warn("[fedapay-webhook] aucun paiement pour tx={}", txId));
        } catch (Exception e) {
            log.error("[fedapay-webhook] payload illisible", e);
            throw new IllegalArgumentException("Payload webhook invalide");
        }
    }

    // ─────────────────────────────────────────────────────────────
    private FraisScolaire fraisInscription(Long classeId, Long anneeScolaireId) {
        TypeFrais type = typeFraisRepository.findByCode(typeFraisInscriptionCode)
                .map(t -> (TypeFrais) t)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Type de frais '" + typeFraisInscriptionCode + "' absent du référentiel — "
                        + "créez-le dans Référentiel ▸ Types de frais."));

        List<FraisScolaire> frais = fraisScolaireRepository
                .findByClasseIdAndAnneeScolaireId(classeId, anneeScolaireId);

        return frais.stream()
                .filter(f -> type.getId().equals(f.getTypeFraisId()))
                .filter(f -> f.getMontant() != null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun frais d'inscription configuré pour cette classe et cette année scolaire."));
    }

    private void appliquerStatut(Paiement paiement, String fedapayStatut) {
        if (paiement.getStatutTransaction() != null && paiement.getStatutTransaction().equals("SUCCES")) {
            return; // idempotent
        }
        switch (fedapayStatut) {
            case "approved", "transferred" -> {
                paiement.setStatutTransaction("SUCCES");
                paiement.setDatePaiement(LocalDate.now());
            }
            case "declined", "canceled", "refunded" -> paiement.setStatutTransaction("ECHEC");
            default -> paiement.setStatutTransaction("INITIE"); // pending
        }
        paiementRepository.save(paiement);
    }

    /**
     * Normalise un numéro béninois au format actuel : 10 chiffres commençant par « 01 ».
     * Ex : "51 10 96 93" → "0151109693" ; "+229 0151109693" → "0151109693".
     */
    private String cleanPhone(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D", "");
        if (digits.startsWith("229")) digits = digits.substring(3);
        if (digits.length() == 8) digits = "01" + digits; // ancien format 8 chiffres
        return digits;
    }

    /**
     * Signature webhook FedaPay (schéma type Stripe) : en-tête « t=timestamp,s=hmac ».
     * hmac = HMAC-SHA256("{t}.{payload}", webhook-secret) en hexadécimal.
     * On accepte aussi un HMAC brut du corps (selon la version FedaPay).
     */
    private void verifierSignature(String rawBody, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("[fedapay-webhook] FEDAPAY_WEBHOOK_SECRET non défini — signature NON vérifiée.");
            return;
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new IllegalArgumentException("Signature webhook absente");
        }
        String t = null, s = null;
        for (String part : signatureHeader.split(",")) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals("t")) t = kv[1];
            if (kv.length == 2 && kv[0].equals("s")) s = kv[1];
        }
        String expectedStripe = (t != null) ? hmacSha256(t + "." + rawBody, webhookSecret) : null;
        String expectedRaw = hmacSha256(rawBody, webhookSecret);
        String provided = (s != null) ? s : signatureHeader.trim();

        if (provided.equalsIgnoreCase(expectedStripe) || provided.equalsIgnoreCase(expectedRaw)) {
            return;
        }
        log.error("[fedapay-webhook] signature invalide (reçu={})", provided);
        throw new IllegalArgumentException("Signature webhook invalide");
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC impossible", e);
        }
    }
}
