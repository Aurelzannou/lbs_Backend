package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.PeriodeRentreeRequest;
import com.App.lbs_backend.dto.response.PeriodeRentreeResponse;
import com.App.lbs_backend.entity.PeriodeRentree;
import com.App.lbs_backend.service.referentiel.PeriodeRentreeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodes-rentree")
@RequiredArgsConstructor
public class PeriodeRentreeController extends MasterController<PeriodeRentree, PeriodeRentreeResponse, PeriodeRentreeRequest> {

    private final PeriodeRentreeService periodeRentreeService;
    private final HttpServletRequest    httpRequest;

    @Override
    protected AbstractBaseService<PeriodeRentree, PeriodeRentreeResponse> service() {
        return periodeRentreeService;
    }

    @Override
    protected PeriodeRentreeResponse doCreate(PeriodeRentreeRequest form) {
        PeriodeRentree entity = new PeriodeRentree();
        applyForm(entity, form);
        PeriodeRentree saved = periodeRentreeService.create(entity);
        return periodeRentreeService.toResponse(saved.getId());
    }

    @Override
    protected PeriodeRentreeResponse doUpdate(String uuid, PeriodeRentreeRequest form) {
        PeriodeRentree entity = periodeRentreeService.findByUuid(uuid);
        applyForm(entity, form);
        periodeRentreeService.update(entity);
        return periodeRentreeService.toResponse(entity.getId());
    }

    @GetMapping("/active/{anneeScolaireId}")
    public ResponseEntity<?> getPeriodeActive(@PathVariable Long anneeScolaireId) {
        return periodeRentreeService.getPeriodeActive(anneeScolaireId)
                .map(p -> ResponseEntity.ok(ApiResponse.apiSuccess("Période active", p, httpRequest.getRequestURI())))
                .orElse(ResponseEntity.ok(ApiResponse.apiSuccess("Aucune période active", null, httpRequest.getRequestURI())));
    }

    private void applyForm(PeriodeRentree entity, PeriodeRentreeRequest form) {
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setLibelle(form.getLibelle());
        entity.setDateOuverture(form.getDateOuverture());
        entity.setDateCloture(form.getDateCloture());
        entity.setActif(form.getActif() != null ? form.getActif() : true);
    }
}
