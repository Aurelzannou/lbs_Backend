package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.ClasseRequest;
import com.App.lbs_backend.dto.response.ClasseResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.service.referentiel.ClasseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClasseController extends MasterController<Classe, ClasseResponse, ClasseRequest> {

    private final ClasseService classeService;

    @Override
    protected AbstractBaseService<Classe, ClasseResponse> service() {
        return classeService;
    }

    @Override
    protected ClasseResponse doCreate(ClasseRequest form) {
        Classe entity = new Classe();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setNiveauId(form.getNiveauId());
        entity.setProfId(form.getProfId());
        entity.setCapaciteMax(form.getCapaciteMax());
        entity.setActif(form.getActif());
        
        Classe saved = classeService.create(entity);
        return classeService.toResponse(saved.getId());
    }

    @Override
    protected ClasseResponse doUpdate(String uuid, ClasseRequest form) {
        Classe entity = classeService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setNiveauId(form.getNiveauId());
        entity.setProfId(form.getProfId());
        entity.setCapaciteMax(form.getCapaciteMax());
        entity.setActif(form.getActif());
        
        classeService.update(entity);
        return classeService.toResponse(entity.getId());
    }
}
