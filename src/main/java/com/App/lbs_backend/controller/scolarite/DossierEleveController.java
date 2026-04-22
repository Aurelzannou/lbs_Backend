package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.DossierEleveRequest;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.service.scolarite.DossierEleveService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dossier-eleves")
@RequiredArgsConstructor
public class DossierEleveController extends MasterController<DossierEleve, DossierEleveResponse, DossierEleveRequest> {

    private final DossierEleveService dossierEleveService;

    @Override
    protected AbstractBaseService<DossierEleve, DossierEleveResponse> service() {
        return dossierEleveService;
    }

    @Override
    protected DossierEleveResponse doCreate(DossierEleveRequest form) {
        DossierEleve dossier = new DossierEleve();
        mapFormToEntity(form, dossier);
        DossierEleve saved = dossierEleveService.create(dossier);
        return dossierEleveService.toResponse(saved.getId());
    }

    @Override
    protected DossierEleveResponse doUpdate(String uuid, DossierEleveRequest form) {
        DossierEleve dossier = dossierEleveService.findByUuid(uuid);
        mapFormToEntity(form, dossier);
        dossierEleveService.update(dossier);
        return dossierEleveService.toResponse(dossier.getId());
    }

    private void mapFormToEntity(DossierEleveRequest form, DossierEleve dossier) {
        dossier.setCode(form.getCode());
        dossier.setEleveId(form.getEleveId());
        dossier.setClasseId(form.getClasseId());
        dossier.setAnneeScolaireId(form.getAnneeScolaireId());
        dossier.setDateDebut(form.getDateDebut());
        dossier.setDateFin(form.getDateFin());
        dossier.setStatutId(form.getStatutId());
        dossier.setEtapeCouranteId(form.getEtapeCouranteId());
        dossier.setRemise(form.getRemise());
        dossier.setNumero(form.getNumero());
        dossier.setTypeOperationId(form.getTypeOperationId());
        dossier.setActeId(form.getActeId());
        dossier.setUtilisateurId(form.getUtilisateurId());
    }
}
