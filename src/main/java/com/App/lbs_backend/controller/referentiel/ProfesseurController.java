package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.ProfesseurRequest;
import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.service.referentiel.ProfesseurService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/professeurs")
@RequiredArgsConstructor
public class ProfesseurController extends MasterController<Professeur, ProfesseurResponse, ProfesseurRequest> {

    private final ProfesseurService professeurService;

    @Override
    protected AbstractBaseService<Professeur, ProfesseurResponse> service() {
        return professeurService;
    }

    @Override
    protected ProfesseurResponse doCreate(ProfesseurRequest form) {
        Professeur entity = new Professeur();
        entity.setCode(form.getCode());
        entity.setNom(form.getNom());
        entity.setPrenom(form.getPrenom());
        entity.setEmail(form.getEmail());
        entity.setResidence(form.getResidence());
        entity.setNum(form.getNum());
        entity.setActif(form.getActif());
        
        Professeur saved = professeurService.create(entity);
        return professeurService.toResponse(saved.getId());
    }

    @Override
    protected ProfesseurResponse doUpdate(String uuid, ProfesseurRequest form) {
        Professeur entity = professeurService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setNom(form.getNom());
        entity.setPrenom(form.getPrenom());
        entity.setEmail(form.getEmail());
        entity.setResidence(form.getResidence());
        entity.setNum(form.getNum());
        entity.setActif(form.getActif());
        
        professeurService.update(entity);
        return professeurService.toResponse(entity.getId());
    }
}
