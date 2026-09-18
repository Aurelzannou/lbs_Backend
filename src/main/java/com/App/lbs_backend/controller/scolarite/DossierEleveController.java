package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.DossierEleveRequest;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.core.utils.ReportService;
import com.App.lbs_backend.service.referentiel.PeriodeInscriptionService;
import com.App.lbs_backend.service.scolarite.DossierEleveService;
import com.App.lbs_backend.service.scolarite.EleveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dossier-eleves")
@RequiredArgsConstructor
public class DossierEleveController extends MasterController<DossierEleve, DossierEleveResponse, DossierEleveRequest> {

    private final DossierEleveService       dossierEleveService;
    private final EleveService              eleveService;
    private final ReportService             reportService;
    private final PeriodeInscriptionService periodeInscriptionService;

    @Override
    protected AbstractBaseService<DossierEleve, DossierEleveResponse> service() {
        return dossierEleveService;
    }

    @Override
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        // criteria.page() est en 1-based (front) — PageRequest.of() attend du 0-based.
        int rawPage   = criteria.page()   != null ? criteria.page()     : 1;
        int page      = Math.max(rawPage - 1, 0);
        int size      = criteria.size()   != null ? criteria.size()     : 10;
        String filter = criteria.filter() != null ? criteria.filter()   : "";
        String anneeIdParam = request.getParameter("anneeId");
        Long anneeId  = anneeIdParam != null ? Long.parseLong(anneeIdParam) : null;
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
            dossierEleveService.searchFilteredResponse(anneeId, filter, pageable), request.getRequestURI()));
    }

    @Override
    protected DossierEleveResponse doCreate(DossierEleveRequest form) {
        // Une nouvelle inscription / réinscription n'est possible que si une période
        // d'inscription est ouverte pour l'année scolaire visée — même règle que le portail
        // parent. Pour une inscription tardive, l'admin élargit la date de clôture de la période.
        if (form.getAnneeScolaireId() != null) {
            periodeInscriptionService.validerPeriode(form.getAnneeScolaireId());
        }

        DossierEleve dossier = new DossierEleve();
        mapFormToEntity(form, dossier);

        // Un enfant ne peut avoir qu'un seul dossier vivant par année scolaire — que ce soit une
        // réinscription (eleveId) ou une nouvelle inscription (identité). Bloqué avant écriture.
        String nomRef = form.getEleveId() != null ? null : form.getNom();
        String prenomRef = form.getEleveId() != null ? null : form.getPrenom();
        if (form.getEleveId() != null) {
            Eleve ref = eleveService.findById(form.getEleveId());
            nomRef = ref.getNom();
            prenomRef = ref.getPrenom();
        }
        dossierEleveService.verifierUnSeulDossierParAnnee(
                form.getEleveId(), nomRef, prenomRef, form.getAnneeScolaireId(), null);

        // L'Eleve n'est créé qu'à l'acceptation du dossier (voir ValidationService.accepter()).
        // Si un élève existant est sélectionné, on recopie son identité pour un affichage
        // cohérent dès le dépôt ; sinon on garde directement les champs saisis dans le formulaire.
        if (form.getEleveId() != null) {
            Eleve existant = eleveService.findById(form.getEleveId());
            dossier.setNom(existant.getNom());
            dossier.setPrenom(existant.getPrenom());
            dossier.setSexe(existant.getSexe());
            dossier.setDateNaissance(existant.getDateNaissance());
            dossier.setSouffrant(existant.getSouffrant());
            dossier.setProvenance(existant.getProvenance());
        }

        if (dossier.getStatutId() == null) {
            dossierEleveService.setStatutDepose(dossier);
        }
        // Numéro de dossier — sur une réinscription le formulaire n'envoie pas nom/prénom
        // (uniquement l'eleveId), on prend donc l'identité déjà recopiée sur le dossier.
        String numero = dossierEleveService.genererNumero(dossier.getNom(), dossier.getPrenom());
        dossier.setNumero(numero);

        DossierEleve saved = dossierEleveService.create(dossier);
        dossierEleveService.enregistrerDepot(saved);
        return dossierEleveService.toResponse(saved.getId());
    }

    @Override
    protected DossierEleveResponse doUpdate(String uuid, DossierEleveRequest form) {
        DossierEleve dossier = dossierEleveService.findByUuid(uuid);
        mapFormToEntity(form, dossier);
        dossierEleveService.update(dossier);
        return dossierEleveService.toResponse(dossier.getId());
    }

    @GetMapping("/tuteur/{tuteurId}")
    public ResponseEntity<List<DossierEleveResponse>> getByTuteur(@PathVariable Long tuteurId) {
        return ResponseEntity.ok(dossierEleveService.getByTuteurId(tuteurId));
    }

    @GetMapping("/{uuid}/fiche-inscription")
    public ResponseEntity<byte[]> getFicheInscription(@PathVariable String uuid) {
        DossierEleveResponse dossier = dossierEleveService.toResponse(
            dossierEleveService.findByUuid(uuid).getId()
        );

        Map<String, Object> params = new HashMap<>();
        params.put("numeroDossier",    dossier.getNumero() != null ? dossier.getNumero() : "—");
        params.put("eleveNom",         dossier.getEleveNom() != null ? dossier.getEleveNom() : "—");
        params.put("elevePrenom",      dossier.getElevePrenom() != null ? dossier.getElevePrenom() : "—");
        params.put("classe",           dossier.getClasseLibelle() != null ? dossier.getClasseLibelle() : "—");
        params.put("anneeScolaire",    dossier.getAnneeScolaireLibelle() != null ? dossier.getAnneeScolaireLibelle() : "—");
        params.put("statut",           dossier.getStatutLibelle() != null ? dossier.getStatutLibelle() : "—");
        params.put("dateDebut",        dossier.getDateDebut() != null ? dossier.getDateDebut().toString() : "—");

        try {
            byte[] pdf = reportService.generatePdfReport("fiche-inscription", params, null);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"fiche-inscription-" + (dossier.getNumero() != null ? dossier.getNumero() : uuid) + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void mapFormToEntity(DossierEleveRequest form, DossierEleve dossier) {
        dossier.setCode(form.getCode());
        dossier.setEleveId(form.getEleveId());
        dossier.setNom(form.getNom());
        dossier.setPrenom(form.getPrenom());
        dossier.setSexe(form.getSexe());
        dossier.setDateNaissance(form.getDateNaissance());
        dossier.setSouffrant(form.getSouffrant());
        dossier.setProvenance(form.getProvenance());
        dossier.setClasseId(form.getClasseId());
        dossier.setAnneeScolaireId(form.getAnneeScolaireId());
        dossier.setDateDebut(form.getDateDebut());
        dossier.setDateFin(form.getDateFin());
        dossier.setStatutId(form.getStatutId());
        dossier.setEtapeCouranteId(form.getEtapeCouranteId());
        dossier.setRemise(form.getRemise());
        dossier.setNumero(form.getNumero());
        dossier.setTypeOperationId(form.getTypeOperationId());
        dossier.setActeId(form.getActeId());
        dossier.setUtilisateurId(form.getUtilisateurId());
    }
}
