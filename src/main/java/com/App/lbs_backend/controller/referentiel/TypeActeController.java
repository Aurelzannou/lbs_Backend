package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.TypeActeRequest;
import com.App.lbs_backend.dto.response.TypeActeResponse;
import com.App.lbs_backend.entity.TypeActe;
import com.App.lbs_backend.service.referentiel.TypeActeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/types-actes")
@RequiredArgsConstructor
public class TypeActeController extends MasterController<TypeActe, TypeActeResponse, TypeActeRequest> {

    private final TypeActeService typeActeService;

    @Override
    protected AbstractBaseService<TypeActe, TypeActeResponse> service() {
        return typeActeService;
    }

    @Override
    protected TypeActeResponse doCreate(TypeActeRequest form) {
        TypeActe entity = new TypeActe();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        
        TypeActe saved = typeActeService.create(entity);
        return typeActeService.toResponse(saved.getId());
    }

    @Override
    protected TypeActeResponse doUpdate(String uuid, TypeActeRequest form) {
        TypeActe entity = typeActeService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        
        typeActeService.update(entity);
        return typeActeService.toResponse(entity.getId());
    }
}
