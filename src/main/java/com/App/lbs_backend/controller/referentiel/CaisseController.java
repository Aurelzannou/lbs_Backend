package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.CaisseRequest;
import com.App.lbs_backend.dto.response.CaisseResponse;
import com.App.lbs_backend.entity.Caisse;
import com.App.lbs_backend.service.referentiel.CaisseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caisses")
@RequiredArgsConstructor
public class CaisseController extends MasterController<Caisse, CaisseResponse, CaisseRequest> {

    private final CaisseService caisseService;

    @Override
    protected AbstractBaseService<Caisse, CaisseResponse> service() {
        return caisseService;
    }

    @Override
    protected CaisseResponse doCreate(CaisseRequest form) {
        Caisse entity = new Caisse();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setSolde(form.getSolde());
        entity.setActif(form.getActif());
        
        Caisse saved = caisseService.create(entity);
        return caisseService.toResponse(saved.getId());
    }

    @Override
    protected CaisseResponse doUpdate(String uuid, CaisseRequest form) {
        Caisse entity = caisseService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setSolde(form.getSolde());
        entity.setActif(form.getActif());
        
        caisseService.update(entity);
        return caisseService.toResponse(entity.getId());
    }
}
