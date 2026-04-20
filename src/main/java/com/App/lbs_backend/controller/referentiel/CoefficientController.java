package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.CoefficientRequest;
import com.App.lbs_backend.dto.response.CoefficientResponse;
import com.App.lbs_backend.entity.Coefficient;
import com.App.lbs_backend.service.referentiel.CoefficientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coefficients")
@RequiredArgsConstructor
public class CoefficientController extends MasterController<Coefficient, CoefficientResponse, CoefficientRequest> {

    private final CoefficientService coefficientService;

    @Override
    protected AbstractBaseService<Coefficient, CoefficientResponse> service() {
        return coefficientService;
    }

    @Override
    protected CoefficientResponse doCreate(CoefficientRequest form) {
        Coefficient entity = new Coefficient();
        entity.setCode(form.getCode());
        entity.setMatiereId(form.getMatiereId());
        entity.setNiveauId(form.getNiveauId());
        entity.setValeur(form.getValeur());
        
        Coefficient saved = coefficientService.create(entity);
        return coefficientService.toResponse(saved.getId());
    }

    @Override
    protected CoefficientResponse doUpdate(String uuid, CoefficientRequest form) {
        Coefficient entity = coefficientService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setMatiereId(form.getMatiereId());
        entity.setNiveauId(form.getNiveauId());
        entity.setValeur(form.getValeur());
        
        coefficientService.update(entity);
        return coefficientService.toResponse(entity.getId());
    }
}
