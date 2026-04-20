package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.TypeFraisRequest;
import com.App.lbs_backend.dto.response.TypeFraisResponse;
import com.App.lbs_backend.entity.TypeFrais;
import com.App.lbs_backend.service.referentiel.TypeFraisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/types-frais")
@RequiredArgsConstructor
public class TypeFraisController extends MasterController<TypeFrais, TypeFraisResponse, TypeFraisRequest> {

    private final TypeFraisService typeFraisService;

    @Override
    protected AbstractBaseService<TypeFrais, TypeFraisResponse> service() {
        return typeFraisService;
    }

    @Override
    protected TypeFraisResponse doCreate(TypeFraisRequest form) {
        TypeFrais entity = new TypeFrais();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setObligatoire(form.getObligatoire());
        entity.setActif(form.getActif());
        
        TypeFrais saved = typeFraisService.create(entity);
        return typeFraisService.toResponse(saved.getId());
    }

    @Override
    protected TypeFraisResponse doUpdate(String uuid, TypeFraisRequest form) {
        TypeFrais entity = typeFraisService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setObligatoire(form.getObligatoire());
        entity.setActif(form.getActif());
        
        typeFraisService.update(entity);
        return typeFraisService.toResponse(entity.getId());
    }
}
