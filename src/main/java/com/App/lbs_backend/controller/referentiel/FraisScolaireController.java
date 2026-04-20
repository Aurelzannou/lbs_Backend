package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.FraisScolaireRequest;
import com.App.lbs_backend.dto.response.FraisScolaireResponse;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.service.referentiel.FraisScolaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/frais-scolaires")
@RequiredArgsConstructor
public class FraisScolaireController extends MasterController<FraisScolaire, FraisScolaireResponse, FraisScolaireRequest> {

    private final FraisScolaireService fraisScolaireService;

    @Override
    protected AbstractBaseService<FraisScolaire, FraisScolaireResponse> service() {
        return fraisScolaireService;
    }

    @Override
    protected FraisScolaireResponse doCreate(FraisScolaireRequest form) {
        FraisScolaire entity = new FraisScolaire();
        entity.setCode(form.getCode());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setClasseId(form.getClasseId());
        entity.setTypeFraisId(form.getTypeFraisId());
        entity.setMontant(form.getMontant());
        entity.setActif(form.getActif());
        
        FraisScolaire saved = fraisScolaireService.create(entity);
        return fraisScolaireService.toResponse(saved.getId());
    }

    @Override
    protected FraisScolaireResponse doUpdate(String uuid, FraisScolaireRequest form) {
        FraisScolaire entity = fraisScolaireService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setClasseId(form.getClasseId());
        entity.setTypeFraisId(form.getTypeFraisId());
        entity.setMontant(form.getMontant());
        entity.setActif(form.getActif());
        
        fraisScolaireService.update(entity);
        return fraisScolaireService.toResponse(entity.getId());
    }
}
