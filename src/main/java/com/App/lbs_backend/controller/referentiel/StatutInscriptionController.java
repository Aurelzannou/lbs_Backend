package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.StatutInscriptionRequest;
import com.App.lbs_backend.dto.response.StatutInscriptionResponse;
import com.App.lbs_backend.entity.StatutInscription;
import com.App.lbs_backend.service.referentiel.StatutInscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statuts-inscriptions")
@RequiredArgsConstructor
public class StatutInscriptionController extends MasterController<StatutInscription, StatutInscriptionResponse, StatutInscriptionRequest> {

    private final StatutInscriptionService statutInscriptionService;

    @Override
    protected AbstractBaseService<StatutInscription, StatutInscriptionResponse> service() {
        return statutInscriptionService;
    }

    @Override
    protected StatutInscriptionResponse doCreate(StatutInscriptionRequest form) {
        StatutInscription entity = new StatutInscription();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        
        StatutInscription saved = statutInscriptionService.create(entity);
        return statutInscriptionService.toResponse(saved.getId());
    }

    @Override
    protected StatutInscriptionResponse doUpdate(String uuid, StatutInscriptionRequest form) {
        StatutInscription entity = statutInscriptionService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        
        statutInscriptionService.update(entity);
        return statutInscriptionService.toResponse(entity.getId());
    }
}
