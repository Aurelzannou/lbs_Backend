package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.EleveTuteurRequest;
import com.App.lbs_backend.dto.response.EleveTuteurResponse;
import com.App.lbs_backend.entity.EleveTuteur;
import com.App.lbs_backend.service.scolarite.EleveTuteurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Association parent(s) ↔ élève. Écran d'administration « Parents & élèves ».
 * Ouvre l'accès portail : un tuteur voit un enfant dès qu'un lien existe ici.
 */
@RestController
@RequestMapping("/api/eleve-tuteurs")
@RequiredArgsConstructor
public class EleveTuteurController extends MasterController<EleveTuteur, EleveTuteurResponse, EleveTuteurRequest> {

    private final EleveTuteurService eleveTuteurService;

    @Override
    protected AbstractBaseService<EleveTuteur, EleveTuteurResponse> service() {
        return eleveTuteurService;
    }

    @Override
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        String eleveIdParam = request.getParameter("eleveId");
        String tuteurIdParam = request.getParameter("tuteurId");
        List<EleveTuteurResponse> result;
        if (eleveIdParam != null) {
            result = eleveTuteurService.listerParEleve(Long.parseLong(eleveIdParam));
        } else if (tuteurIdParam != null) {
            result = eleveTuteurService.listerParTuteur(Long.parseLong(tuteurIdParam));
        } else {
            result = Collections.emptyList();
        }
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", result, request.getRequestURI()));
    }

    @Override
    protected EleveTuteurResponse doCreate(EleveTuteurRequest form) {
        return eleveTuteurService.associer(form);
    }

    @Override
    protected EleveTuteurResponse doUpdate(String uuid, EleveTuteurRequest form) {
        return eleveTuteurService.modifier(uuid, form);
    }

    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        return eleveTuteurService.delete(uuids);
    }
}
