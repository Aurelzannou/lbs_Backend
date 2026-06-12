package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.PeriodeInscriptionRequest;
import com.App.lbs_backend.dto.response.PeriodeInscriptionResponse;
import com.App.lbs_backend.entity.PeriodeInscription;
import com.App.lbs_backend.service.referentiel.PeriodeInscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodes-inscription")
@RequiredArgsConstructor
public class PeriodeInscriptionController extends MasterController<PeriodeInscription, PeriodeInscriptionResponse, PeriodeInscriptionRequest> {

    private final PeriodeInscriptionService periodeInscriptionService;
    private final HttpServletRequest        httpRequest;

    @Override
    protected AbstractBaseService<PeriodeInscription, PeriodeInscriptionResponse> service() {
        return periodeInscriptionService;
    }

    @Override
    protected PeriodeInscriptionResponse doCreate(PeriodeInscriptionRequest form) {
        PeriodeInscription entity = new PeriodeInscription();
        applyForm(entity, form);
        PeriodeInscription saved = periodeInscriptionService.create(entity);
        return periodeInscriptionService.toResponse(saved.getId());
    }

    @Override
    protected PeriodeInscriptionResponse doUpdate(String uuid, PeriodeInscriptionRequest form) {
        PeriodeInscription entity = periodeInscriptionService.findByUuid(uuid);
        applyForm(entity, form);
        periodeInscriptionService.update(entity);
        return periodeInscriptionService.toResponse(entity.getId());
    }

    @GetMapping("/active/{anneeScolaireId}")
    public ResponseEntity<?> getPeriodeActive(@PathVariable Long anneeScolaireId) {
        return periodeInscriptionService.getPeriodeActive(anneeScolaireId)
                .map(p -> ResponseEntity.ok(ApiResponse.apiSuccess("Période active", p, httpRequest.getRequestURI())))
                .orElse(ResponseEntity.ok(ApiResponse.apiSuccess("Aucune période active", null, httpRequest.getRequestURI())));
    }

    private void applyForm(PeriodeInscription entity, PeriodeInscriptionRequest form) {
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setLibelle(form.getLibelle());
        entity.setDateOuverture(form.getDateOuverture());
        entity.setDateCloture(form.getDateCloture());
        entity.setActif(form.getActif() != null ? form.getActif() : true);
    }
}
