package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.core.utils.ReportService;
import com.App.lbs_backend.dto.request.PaiementRequest;
import com.App.lbs_backend.dto.response.PaiementResponse;
import com.App.lbs_backend.dto.response.SuiviPaiementResponse;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.repository.UtilisateurRepository;
import com.App.lbs_backend.service.scolarite.CaisseMouvementService;
import com.App.lbs_backend.service.scolarite.PaiementService;
import com.App.lbs_backend.service.scolarite.SuiviPaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController extends MasterController<Paiement, PaiementResponse, PaiementRequest> {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_CAISSIER = "ROLE_CAISSIER";

    private final PaiementService paiementService;
    private final CaisseMouvementService caisseMouvementService;
    private final SuiviPaiementService suiviPaiementService;
    private final ReportService reportService;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    protected AbstractBaseService<Paiement, PaiementResponse> service() {
        return paiementService;
    }

    @Override
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        int rawPage = criteria.page() != null ? criteria.page() : 1;
        int page = Math.max(rawPage - 1, 0);
        int size = criteria.size() != null ? criteria.size() : 10;
        String filter = criteria.filter() != null ? criteria.filter() : "";

        String anneeIdParam = request.getParameter("anneeScolaireId");
        Long anneeScolaireId = anneeIdParam != null ? Long.parseLong(anneeIdParam) : null;

        // Un caissier (rôle CAISSIER, sans le rôle ADMIN) ne voit que les paiements qu'il a
        // lui-même enregistrés — l'admin garde une vue globale sur tous les caissiers.
        Long utilisateurId = estCaissierNonAdmin() ? utilisateurCourantIdOuIntrouvable() : null;

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
            paiementService.searchFilteredResponse(filter, anneeScolaireId, utilisateurId, pageable), request.getRequestURI()));
    }

    @Override
    protected PaiementResponse doCreate(PaiementRequest form) {
        // On n'encaisse jamais plus que le reste à payer du frais concerné.
        paiementService.verifierPasDeSurpaiement(
                form.getDossierEleveId(), form.getFraisScolaireId(), form.getMontant());

        Paiement paiement = new Paiement();
        mapFormToEntity(form, paiement);
        paiement.setCanal(form.getCanal() != null ? form.getCanal() : "SUR_PLACE");
        // Saisie admin/caissier : validée immédiatement, aucune passerelle de paiement en ligne branchée.
        paiement.setStatutTransaction("SUCCES");
        paiement.setCode("PAI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        // Le caissier réellement authentifié est toujours celui qui encaisse — jamais une valeur
        // envoyée par le client — pour que la liste "mes paiements" reste fiable.
        paiement.setUtilisateurId(utilisateurCourantId());

        Paiement saved = paiementService.create(paiement);
        caisseMouvementService.enregistrerMouvement(
                saved.getCaisseId(), CaisseMouvementService.ENTREE, saved.getMontant(),
                "PAIEMENT", saved.getId(), "Paiement " + saved.getCode());

        return paiementService.toResponse(saved.getId());
    }

    /** true si l'utilisateur connecté a le rôle CAISSIER sans avoir aussi le rôle ADMIN
        (un admin qui serait aussi caissier garde la vue globale). */
    private boolean estCaissierNonAdmin() {
        return aRole(ROLE_CAISSIER) && !aRole(ROLE_ADMIN);
    }

    private boolean aRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }

    /** Utilisateur connecté, résolu via son identifiant Keycloak (jwt "sub"). */
    private Long utilisateurCourantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) return null;
        return utilisateurRepository.findByKeycloack(jwtAuth.getToken().getSubject())
                .map(Utilisateur::getId)
                .orElse(null);
    }

    /** Variante utilisée pour le filtrage "mes paiements" : si l'utilisateur connecté ne peut
        pas être résolu, on renvoie un id garanti sans correspondance plutôt que null (qui
        lèverait le filtre et exposerait tous les paiements). */
    private Long utilisateurCourantIdOuIntrouvable() {
        Long id = utilisateurCourantId();
        return id != null ? id : -1L;
    }

    @Override
    protected PaiementResponse doUpdate(String uuid, PaiementRequest form) {
        throw new IllegalArgumentException("Un paiement ne peut pas être modifié une fois enregistré — utilisez l'annulation.");
    }

    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        throw new IllegalArgumentException("Un paiement ne peut pas être supprimé — utilisez l'annulation.");
    }

    @PutMapping("/{uuid}/annuler")
    public ResponseEntity<?> annuler(@PathVariable String uuid) {
        PaiementResponse dto = paiementService.annuler(uuid);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Paiement annulé", dto, request.getRequestURI()));
    }

    @GetMapping("/{uuid}/recu-pdf")
    public ResponseEntity<byte[]> getRecuPdf(@PathVariable String uuid) {
        Paiement paiement = paiementService.findByUuid(uuid);
        PaiementResponse dto = paiementService.toResponse(paiement.getId());
        SuiviPaiementResponse suivi = paiement.getDossierEleveId() != null
                ? suiviPaiementService.getSuiviParDossier(paiement.getDossierEleveId())
                : null;

        Map<String, Object> params = new HashMap<>();
        params.put("code", dto.code() != null ? dto.code() : "—");
        params.put("reference", dto.reference() != null ? dto.reference() : "—");
        params.put("eleveNom", dto.dossierEleve() != null ? dto.dossierEleve().getEleveNom() : "—");
        params.put("elevePrenom", dto.dossierEleve() != null ? dto.dossierEleve().getElevePrenom() : "—");
        params.put("classe", dto.dossierEleve() != null ? dto.dossierEleve().getClasseLibelle() : "—");
        params.put("frais", dto.fraisScolaire() != null && dto.fraisScolaire().typeFrais() != null
                ? dto.fraisScolaire().typeFrais().libelle() : "—");
        params.put("datePaiement", dto.datePaiement() != null ? dto.datePaiement().toString() : "—");
        params.put("montant", dto.montant() != null ? dto.montant() : 0.0);
        params.put("modePaiement", dto.modePaiement() != null ? dto.modePaiement().libelle() : "—");
        params.put("caisse", dto.caisse() != null ? dto.caisse().libelle() : "—");
        params.put("resteAPayer", suivi != null ? suivi.totalReste() : null);

        try {
            byte[] pdf = reportService.generatePdfReport("recu", params, null);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"recu-" + (dto.code() != null ? dto.code() : uuid) + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void mapFormToEntity(PaiementRequest form, Paiement paiement) {
        paiement.setReference(form.getReference());
        paiement.setDossierEleveId(form.getDossierEleveId());
        paiement.setFraisScolaireId(form.getFraisScolaireId());
        paiement.setDatePaiement(form.getDatePaiement());
        paiement.setMontant(form.getMontant());
        paiement.setModePaiementId(form.getModePaiementId());
        paiement.setCaisseId(form.getCaisseId());
        paiement.setUtilisateurId(form.getUtilisateurId());
        paiement.setObservation(form.getObservation());
        paiement.setTelephonePaiement(form.getTelephonePaiement());
    }
}
