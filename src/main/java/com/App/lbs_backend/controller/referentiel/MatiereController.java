package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.MatiereRequest;
import com.App.lbs_backend.dto.response.MatiereResponse;
import com.App.lbs_backend.entity.Matiere;
import com.App.lbs_backend.service.referentiel.MatiereService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matieres")
@RequiredArgsConstructor
public class MatiereController extends MasterController<Matiere, MatiereResponse, MatiereRequest> {

    private final MatiereService matiereService;

    @Override
    protected AbstractBaseService<Matiere, MatiereResponse> service() {
        return matiereService;
    }

    @Override
    protected MatiereResponse doCreate(MatiereRequest form) {
        Matiere entity = new Matiere();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setActif(form.getActif());
        
        Matiere saved = matiereService.create(entity);
        return matiereService.toResponse(saved.getId());
    }

    @Override
    protected MatiereResponse doUpdate(String uuid, MatiereRequest form) {
        Matiere entity = matiereService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setActif(form.getActif());
        
        matiereService.update(entity);
        return matiereService.toResponse(entity.getId());
    }
}
