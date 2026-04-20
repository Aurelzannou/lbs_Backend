package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.ActeRequest;
import com.App.lbs_backend.dto.response.ActeResponse;
import com.App.lbs_backend.entity.Acte;
import com.App.lbs_backend.service.referentiel.ActeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actes")
@RequiredArgsConstructor
public class ActeController extends MasterController<Acte, ActeResponse, ActeRequest> {

    private final ActeService acteService;

    @Override
    protected AbstractBaseService<Acte, ActeResponse> service() {
        return acteService;
    }

    @Override
    protected ActeResponse doCreate(ActeRequest form) {
        Acte entity = new Acte();
        entity.setCode(form.getCode());
        entity.setReference(form.getReference());
        entity.setTypeActeId(form.getTypeActeId());
        entity.setCheminFichier(form.getCheminFichier());
        entity.setNomFichier(form.getNomFichier());
        
        Acte saved = acteService.create(entity);
        return acteService.toResponse(saved.getId());
    }

    @Override
    protected ActeResponse doUpdate(String uuid, ActeRequest form) {
        Acte entity = acteService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setReference(form.getReference());
        entity.setTypeActeId(form.getTypeActeId());
        entity.setCheminFichier(form.getCheminFichier());
        entity.setNomFichier(form.getNomFichier());
        
        acteService.update(entity);
        return acteService.toResponse(entity.getId());
    }
}
