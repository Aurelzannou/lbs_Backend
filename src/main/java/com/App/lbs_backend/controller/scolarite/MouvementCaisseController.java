package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.MouvementCaisseRequest;
import com.App.lbs_backend.dto.response.MouvementCaisseResponse;
import com.App.lbs_backend.entity.MouvementCaisse;
import com.App.lbs_backend.service.scolarite.MouvementCaisseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Journal de caisse — lecture seule. Toute écriture passe exclusivement par CaisseMouvementService
 * (appelé depuis PaiementController/DepenseScolaireController) ; création/modification/suppression
 * directes via l'API sont volontairement refusées pour préserver l'intégrité du journal.
 */
@RestController
@RequestMapping("/api/mouvements-caisse")
@RequiredArgsConstructor
public class MouvementCaisseController extends MasterController<MouvementCaisse, MouvementCaisseResponse, MouvementCaisseRequest> {

    private final MouvementCaisseService mouvementCaisseService;

    @Override
    protected AbstractBaseService<MouvementCaisse, MouvementCaisseResponse> service() {
        return mouvementCaisseService;
    }

    @Override
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        String caisseIdParam = request.getParameter("caisseId");
        List<MouvementCaisseResponse> result = caisseIdParam == null
                ? Collections.emptyList()
                : mouvementCaisseService.listerParCaisse(Long.parseLong(caisseIdParam));
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", result, request.getRequestURI()));
    }

    @Override
    protected MouvementCaisseResponse doCreate(MouvementCaisseRequest form) {
        throw new IllegalArgumentException("Le journal de caisse ne peut pas être modifié directement.");
    }

    @Override
    protected MouvementCaisseResponse doUpdate(String uuid, MouvementCaisseRequest form) {
        throw new IllegalArgumentException("Le journal de caisse ne peut pas être modifié directement.");
    }
}
