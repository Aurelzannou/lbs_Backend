package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.EmploiDuTempsRequest;
import com.App.lbs_backend.dto.request.ReorganiserJourRequest;
import com.App.lbs_backend.dto.response.EmploiDuTempsResponse;
import com.App.lbs_backend.entity.EmploiDuTemps;
import com.App.lbs_backend.service.referentiel.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emploi-du-temps")
@RequiredArgsConstructor
public class EmploiDuTempsController extends MasterController<EmploiDuTemps, EmploiDuTempsResponse, EmploiDuTempsRequest> {

    private final EmploiDuTempsService emploiDuTempsService;

    @Override
    protected AbstractBaseService<EmploiDuTemps, EmploiDuTempsResponse> service() {
        return emploiDuTempsService;
    }

    @Override
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        String classeIdParam = request.getParameter("classeId");
        String anneeIdParam = request.getParameter("anneeScolaireId");
        List<EmploiDuTempsResponse> result = (classeIdParam == null || anneeIdParam == null)
                ? Collections.emptyList()
                : emploiDuTempsService.listerResponses(
                        Long.parseLong(classeIdParam), Long.parseLong(anneeIdParam));
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", result, request.getRequestURI()));
    }

    @Override
    protected EmploiDuTempsResponse doCreate(EmploiDuTempsRequest form) {
        emploiDuTempsService.verifierAnneeModifiable(form.getAnneeScolaireId());
        emploiDuTempsService.verifierConflits(
                form.getClasseId(), form.getAnneeScolaireId(), form.getProfId(), form.getJour(),
                form.getHeureDebut(), form.getHeureFin(), null);

        EmploiDuTemps entity = new EmploiDuTemps();
        mapFormToEntity(form, entity);
        entity.setCode("EDT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        EmploiDuTemps saved = emploiDuTempsService.create(entity);
        return emploiDuTempsService.toResponse(saved.getId());
    }

    @Override
    protected EmploiDuTempsResponse doUpdate(String uuid, EmploiDuTempsRequest form) {
        EmploiDuTemps entity = emploiDuTempsService.findByUuid(uuid);

        emploiDuTempsService.verifierAnneeModifiable(entity.getAnneeScolaireId());
        emploiDuTempsService.verifierConflits(
                form.getClasseId(), form.getAnneeScolaireId(), form.getProfId(), form.getJour(),
                form.getHeureDebut(), form.getHeureFin(), entity.getId());

        mapFormToEntity(form, entity);
        emploiDuTempsService.update(entity);
        return emploiDuTempsService.toResponse(entity.getId());
    }

    /** Brouillon PDF (imprimable) de l'emploi du temps de la classe sélectionnée. */
    @GetMapping("/classe/{classeId}/pdf")
    public ResponseEntity<byte[]> exporterBrouillonPdf(
            @PathVariable Long classeId, @RequestParam Long anneeScolaireId) {
        byte[] pdf = emploiDuTempsService.genererBrouillonPdf(classeId, anneeScolaireId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"emploi-du-temps-classe-" + classeId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/count-prof")
    public ResponseEntity<?> countParProf(@RequestParam Long profId) {
        long count = emploiDuTempsService.countCoursParProf(profId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", count, request.getRequestURI()));
    }

    @PutMapping("/reorganiser-jour")
    public ResponseEntity<?> reorganiserJour(@RequestBody ReorganiserJourRequest form) {
        List<EmploiDuTempsResponse> result = emploiDuTempsService.reorganiserJour(form).stream()
                .map(e -> emploiDuTempsService.mapper().toResponse(e))
                .toList();
        return ResponseEntity.ok(ApiResponse.apiSuccess("Emploi du temps réorganisé", result, request.getRequestURI()));
    }

    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        for (String uuid : uuids.ids()) {
            EmploiDuTemps entity = emploiDuTempsService.findByUuid(uuid);
            emploiDuTempsService.verifierAnneeModifiable(entity.getAnneeScolaireId());
        }
        return emploiDuTempsService.delete(uuids);
    }

    private void mapFormToEntity(EmploiDuTempsRequest form, EmploiDuTemps entity) {
        entity.setClasseId(form.getClasseId());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setMatiereId(form.getMatiereId());
        entity.setProfId(form.getProfId());
        entity.setJour(form.getJour());
        entity.setHeureDebut(form.getHeureDebut());
        entity.setHeureFin(form.getHeureFin());
    }
}
