package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.TuteurRequest;
import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.service.scolarite.TuteurService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lecture seule : sert le sélecteur « rechercher un parent » de l'écran Parents & élèves.
 * Les comptes parents se créent via l'inscription en ligne (self-service), pas ici.
 */
@RestController
@RequestMapping("/api/tuteurs")
@RequiredArgsConstructor
public class TuteurController extends MasterController<Tuteur, TuteurResponse, TuteurRequest> {

    private final TuteurService tuteurService;

    @Override
    protected AbstractBaseService<Tuteur, TuteurResponse> service() {
        return tuteurService;
    }

    @Override
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        int rawPage = criteria.page() != null ? criteria.page() : 1;
        int page = Math.max(rawPage - 1, 0);
        int size = criteria.size() != null ? criteria.size() : 20;
        String filter = criteria.filter() != null ? criteria.filter() : "";
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
                tuteurService.rechercher(filter, pageable), request.getRequestURI()));
    }

    @Override
    protected TuteurResponse doCreate(TuteurRequest form) {
        throw new IllegalArgumentException(
                "Les comptes parents se créent depuis l'inscription en ligne, pas ici.");
    }

    @Override
    protected TuteurResponse doUpdate(String uuid, TuteurRequest form) {
        throw new IllegalArgumentException(
                "La modification d'un compte parent n'est pas disponible ici.");
    }

    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        throw new IllegalArgumentException("Un compte parent ne peut pas être supprimé ici.");
    }
}
