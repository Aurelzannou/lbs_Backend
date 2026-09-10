package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.PaiementInitRequest;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.service.paiement.PaiementInscriptionService;
import com.App.lbs_backend.service.scolarite.SuiviPaiementService;
import com.App.lbs_backend.service.scolarite.TuteurLienService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Portail parent — consultation du détail des frais / tranches d'un enfant et paiement en ligne
 * (scolarité, etc.) via FedaPay. Chaque endpoint vérifie que le dossier appartient bien au
 * tuteur connecté.
 */
@RestController
@RequestMapping("/api/portail")
@RequiredArgsConstructor
public class PortailPaiementController {

    private final SuiviPaiementService suiviPaiementService;
    private final PaiementInscriptionService paiementService;
    private final DossierEleveRepository dossierEleveRepository;
    private final TuteurRepository tuteurRepository;
    private final TuteurLienService tuteurLienService;
    private final HttpServletRequest request;

    /** Détail des frais + tranches + reste à payer pour un dossier de l'enfant. */
    @GetMapping("/dossiers/{dossierId}/suivi")
    public ResponseEntity<?> suivi(@PathVariable Long dossierId, @AuthenticationPrincipal Jwt jwt) {
        verifierProprietaire(dossierId, jwt);
        // exclureInscription = true : les frais d'inscription se paient pendant le parcours
        // d'inscription, pas dans cet écran.
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
                suiviPaiementService.getSuiviParDossier(dossierId, true), request.getRequestURI()));
    }

    /** Lance le paiement en ligne (solde du restant dû) d'un frais scolaire de l'enfant. */
    @PostMapping("/dossiers/{dossierId}/frais/{fraisId}/paiement/init")
    public ResponseEntity<?> initPaiement(@PathVariable Long dossierId, @PathVariable Long fraisId,
                                          @RequestBody(required = false) PaiementInitRequest body,
                                          @AuthenticationPrincipal Jwt jwt) {
        verifierProprietaire(dossierId, jwt);
        String tel = body != null ? body.getTelephonePaiement() : null;
        Long montant = body != null ? body.getMontant() : null;
        return ResponseEntity.ok(ApiResponse.apiSuccess("Paiement initialisé",
                paiementService.initPourFrais(dossierId, fraisId, montant, tel), request.getRequestURI()));
    }

    private void verifierProprietaire(Long dossierId, Jwt jwt) {
        String email = jwt == null ? null
                : (jwt.getClaimAsString("email") != null
                    ? jwt.getClaimAsString("email")
                    : jwt.getClaimAsString("preferred_username"));

        Long tuteurId = email == null ? null
                : tuteurRepository.findByEmail(email).map(Tuteur::getId).orElse(null);

        DossierEleve dossier = dossierEleveRepository.findById(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable."));

        boolean autorise = tuteurId != null && (
                tuteurId.equals(dossier.getTuteurId())
                || (dossier.getEleveId() != null
                    && tuteurLienService.eleveIdsDuTuteur(tuteurId).contains(dossier.getEleveId())));
        if (!autorise) {
            throw new IllegalArgumentException(
                    "Accès refusé : ce dossier n'est pas rattaché à votre compte.");
        }
    }
}
