package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.PeriodeAcademiqueRequest;
import com.App.lbs_backend.dto.response.PeriodeAcademiqueResponse;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.service.referentiel.PeriodeAcademiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/periodes-academiques")
@RequiredArgsConstructor
public class PeriodeAcademiqueController extends MasterController<PeriodeAcademique, PeriodeAcademiqueResponse, PeriodeAcademiqueRequest> {

    private final PeriodeAcademiqueService periodeAcademiqueService;

    @Override
    protected AbstractBaseService<PeriodeAcademique, PeriodeAcademiqueResponse> service() {
        return periodeAcademiqueService;
    }

    @Override
    protected PeriodeAcademiqueResponse doCreate(PeriodeAcademiqueRequest form) {
        PeriodeAcademique entity = new PeriodeAcademique();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setDateDebut(form.getDateDebut());
        entity.setDateFin(form.getDateFin());
        entity.setVerrouille(form.getVerrouille());
        
        PeriodeAcademique saved = periodeAcademiqueService.create(entity);
        return periodeAcademiqueService.toResponse(saved.getId());
    }

    @Override
    protected PeriodeAcademiqueResponse doUpdate(String uuid, PeriodeAcademiqueRequest form) {
        PeriodeAcademique entity = periodeAcademiqueService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setDateDebut(form.getDateDebut());
        entity.setDateFin(form.getDateFin());
        entity.setVerrouille(form.getVerrouille());
        
        periodeAcademiqueService.update(entity);
        return periodeAcademiqueService.toResponse(entity.getId());
    }
}
