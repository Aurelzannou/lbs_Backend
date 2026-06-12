package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.DossierEleveRequest;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.core.utils.ReportService;
import com.App.lbs_backend.service.scolarite.DossierEleveService;
import lombok.RequiredArgsConstructor;
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

    private final DossierEleveService dossierEleveService;
    private final ReportService       reportService;

    @Override
    protected AbstractBaseService<DossierEleve, DossierEleveResponse> service() {
        return dossierEleveService;
    }

    @Override
    protected DossierEleveResponse doCreate(DossierEleveRequest form) {
        DossierEleve dossier = new DossierEleve();
        mapFormToEntity(form, dossier);
        // Si aucun statut fourni, auto-set DEPOSE
        if (dossier.getStatutId() == null) {
            dossierEleveService.setStatutDepose(dossier);
        }
        DossierEleve saved = dossierEleveService.create(dossier);
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
