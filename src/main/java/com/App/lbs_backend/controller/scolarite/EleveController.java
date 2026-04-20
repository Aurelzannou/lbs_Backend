package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.EleveRequest;
import com.App.lbs_backend.dto.response.EleveResponse;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.service.scolarite.EleveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eleves")
@RequiredArgsConstructor
public class EleveController extends MasterController<Eleve, EleveResponse, EleveRequest> {

    private final EleveService eleveService;

    @Override
    protected AbstractBaseService<Eleve, EleveResponse> service() {
        return eleveService;
    }

    @Override
    protected EleveResponse doCreate(EleveRequest form) {
        Eleve eleve = new Eleve();
        mapFormToEntity(form, eleve);
        Eleve saved = eleveService.create(eleve);
        return eleveService.toResponse(saved.getId());
    }

    @Override
    protected EleveResponse doUpdate(String uuid, EleveRequest form) {
        Eleve eleve = eleveService.findByUuid(uuid);
        mapFormToEntity(form, eleve);
        eleveService.update(eleve);
        return eleveService.toResponse(eleve.getId());
    }

    private void mapFormToEntity(EleveRequest form, Eleve eleve) {
        eleve.setCode(form.code());
        eleve.setNom(form.nom());
        eleve.setPrenom(form.prenom());
        eleve.setSexe(form.sexe());
        eleve.setDateNaissance(form.dateNaissance());
        eleve.setMatricule(form.matricule());
        eleve.setActif(form.actif());
        eleve.setSouffrant(form.souffrant());
        eleve.setProvenance(form.provenance());
        eleve.setPhoto(form.photo());
        eleve.setUtilisateurId(form.utilisateurId());
    }
}
