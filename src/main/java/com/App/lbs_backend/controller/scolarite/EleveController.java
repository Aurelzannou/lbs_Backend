package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.EleveRequest;
import com.App.lbs_backend.dto.response.EleveResponse;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.service.scolarite.EleveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        int page      = criteria.page()   != null ? Math.max(criteria.page(), 0) : 0;
        int size      = criteria.size()   != null ? criteria.size()     : 10;
        String filter = criteria.filter() != null ? criteria.filter()   : "";
        String classeIdParam = request.getParameter("classeId");
        Long classeId = classeIdParam != null ? Long.parseLong(classeIdParam) : null;
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Eleve> result = eleveService.searchFiltered(classeId, filter, pageable);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
            eleveService.toPageResponse(result), request.getRequestURI()));
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
        eleve.setActif(form.actif());
        eleve.setSouffrant(form.souffrant());
        eleve.setProvenance(form.provenance());
        eleve.setPhoto(form.photo());
        eleve.setUtilisateurId(form.utilisateurId());
        eleve.setClasseId(form.classeId());
        eleve.setTuteurId(form.tuteurId());
    }
}
