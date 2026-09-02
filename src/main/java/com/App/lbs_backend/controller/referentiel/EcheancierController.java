package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.EcheancierRequest;
import com.App.lbs_backend.dto.response.EcheancierResponse;
import com.App.lbs_backend.entity.Echeancier;
import com.App.lbs_backend.service.referentiel.EcheancierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/echeanciers")
@RequiredArgsConstructor
public class EcheancierController extends MasterController<Echeancier, EcheancierResponse, EcheancierRequest> {

    private final EcheancierService echeancierService;

    @Override
    protected AbstractBaseService<Echeancier, EcheancierResponse> service() {
        return echeancierService;
    }

    @Override
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        String fraisScolaireIdParam = request.getParameter("fraisScolaireId");
        List<EcheancierResponse> result = fraisScolaireIdParam == null
                ? Collections.emptyList()
                : echeancierService.listerParFraisScolaire(Long.parseLong(fraisScolaireIdParam));
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", result, request.getRequestURI()));
    }

    @Override
    protected EcheancierResponse doCreate(EcheancierRequest form) {
        echeancierService.verifierTotalTranches(form.getFraisScolaireId(), form.getMontant(), null);

        Echeancier entity = new Echeancier();
        mapFormToEntity(form, entity);
        entity.setCode("ECH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        Echeancier saved = echeancierService.create(entity);
        return echeancierService.toResponse(saved.getId());
    }

    @Override
    protected EcheancierResponse doUpdate(String uuid, EcheancierRequest form) {
        Echeancier entity = echeancierService.findByUuid(uuid);
        echeancierService.verifierTotalTranches(form.getFraisScolaireId(), form.getMontant(), uuid);
        mapFormToEntity(form, entity);
        echeancierService.update(entity);
        return echeancierService.toResponse(entity.getId());
    }

    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        return echeancierService.delete(uuids);
    }

    private void mapFormToEntity(EcheancierRequest form, Echeancier entity) {
        entity.setFraisScolaireId(form.getFraisScolaireId());
        entity.setNumero(form.getNumero());
        entity.setLibelle(form.getLibelle());
        entity.setDateEcheance(form.getDateEcheance());
        entity.setMontant(form.getMontant());
    }
}
