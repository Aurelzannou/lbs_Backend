package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.CategorieDepenseRequest;
import com.App.lbs_backend.dto.response.CategorieDepenseResponse;
import com.App.lbs_backend.entity.CategorieDepense;
import com.App.lbs_backend.service.referentiel.CategorieDepenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories-depenses")
@RequiredArgsConstructor
public class CategorieDepenseController extends MasterController<CategorieDepense, CategorieDepenseResponse, CategorieDepenseRequest> {

    private final CategorieDepenseService categorieDepenseService;

    @Override
    protected AbstractBaseService<CategorieDepense, CategorieDepenseResponse> service() {
        return categorieDepenseService;
    }

    @Override
    protected CategorieDepenseResponse doCreate(CategorieDepenseRequest form) {
        CategorieDepense entity = new CategorieDepense();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setActif(form.getActif());
        
        CategorieDepense saved = categorieDepenseService.create(entity);
        return categorieDepenseService.toResponse(saved.getId());
    }

    @Override
    protected CategorieDepenseResponse doUpdate(String uuid, CategorieDepenseRequest form) {
        CategorieDepense entity = categorieDepenseService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setActif(form.getActif());
        
        categorieDepenseService.update(entity);
        return categorieDepenseService.toResponse(entity.getId());
    }
}
