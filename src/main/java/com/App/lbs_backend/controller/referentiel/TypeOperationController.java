package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.TypeOperationRequest;
import com.App.lbs_backend.dto.response.TypeOperationResponse;
import com.App.lbs_backend.entity.TypeOperation;
import com.App.lbs_backend.service.referentiel.TypeOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/types-operations")
@RequiredArgsConstructor
public class TypeOperationController extends MasterController<TypeOperation, TypeOperationResponse, TypeOperationRequest> {

    private final TypeOperationService typeOperationService;

    @Override
    protected AbstractBaseService<TypeOperation, TypeOperationResponse> service() {
        return typeOperationService;
    }

    @Override
    protected TypeOperationResponse doCreate(TypeOperationRequest form) {
        TypeOperation entity = new TypeOperation();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setDescription(form.getDescription());
        
        TypeOperation saved = typeOperationService.create(entity);
        return typeOperationService.toResponse(saved.getId());
    }

    @Override
    protected TypeOperationResponse doUpdate(String uuid, TypeOperationRequest form) {
        TypeOperation entity = typeOperationService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setDescription(form.getDescription());
        
        typeOperationService.update(entity);
        return typeOperationService.toResponse(entity.getId());
    }
}
