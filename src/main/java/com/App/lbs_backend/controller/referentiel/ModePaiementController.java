package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.ModePaiementRequest;
import com.App.lbs_backend.dto.response.ModePaiementResponse;
import com.App.lbs_backend.entity.ModePaiement;
import com.App.lbs_backend.service.referentiel.ModePaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/modes-paiements")
@RequiredArgsConstructor
public class ModePaiementController extends MasterController<ModePaiement, ModePaiementResponse, ModePaiementRequest> {

    private final ModePaiementService modePaiementService;

    @Override
    protected AbstractBaseService<ModePaiement, ModePaiementResponse> service() {
        return modePaiementService;
    }

    @Override
    protected ModePaiementResponse doCreate(ModePaiementRequest form) {
        ModePaiement entity = new ModePaiement();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setActif(form.getActif());
        
        ModePaiement saved = modePaiementService.create(entity);
        return modePaiementService.toResponse(saved.getId());
    }

    @Override
    protected ModePaiementResponse doUpdate(String uuid, ModePaiementRequest form) {
        ModePaiement entity = modePaiementService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setActif(form.getActif());
        
        modePaiementService.update(entity);
        return modePaiementService.toResponse(entity.getId());
    }
}
