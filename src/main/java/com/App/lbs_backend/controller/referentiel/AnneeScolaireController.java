package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.AnneeScolaireRequest;
import com.App.lbs_backend.dto.response.AnneeScolaireResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.service.referentiel.AnneeScolaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/annees-scolaires")
@RequiredArgsConstructor
public class AnneeScolaireController extends MasterController<AnneeScolaire, AnneeScolaireResponse, AnneeScolaireRequest> {

    private final AnneeScolaireService anneeScolaireService;

    @Override
    protected AbstractBaseService<AnneeScolaire, AnneeScolaireResponse> service() {
        return anneeScolaireService;
    }

    @Override
    protected AnneeScolaireResponse doCreate(AnneeScolaireRequest form) {
        anneeScolaireService.verifierChevauchement(form.getDateDebut(), form.getDateFin(), null);
        AnneeScolaire entity = new AnneeScolaire();
        applyForm(entity, form);
        AnneeScolaire saved = anneeScolaireService.create(entity);
        anneeScolaireService.assurerActiviteUnique(saved);
        return anneeScolaireService.toResponse(saved.getId());
    }

    @Override
    protected AnneeScolaireResponse doUpdate(String uuid, AnneeScolaireRequest form) {
        AnneeScolaire entity = anneeScolaireService.findByUuid(uuid);
        anneeScolaireService.verifierChevauchement(form.getDateDebut(), form.getDateFin(), entity.getId());
        applyForm(entity, form);
        anneeScolaireService.update(entity);
        anneeScolaireService.assurerActiviteUnique(entity);
        return anneeScolaireService.toResponse(entity.getId());
    }

    private void applyForm(AnneeScolaire entity, AnneeScolaireRequest form) {
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setDateDebut(form.getDateDebut());
        entity.setDateFin(form.getDateFin());
        entity.setActif(form.getActif());
    }
}
