package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.specs.PaginationCriteria;
import com.App.lbs_backend.dto.request.PeriodeAcademiqueRequest;
import com.App.lbs_backend.dto.response.PeriodeAcademiqueResponse;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.service.referentiel.PeriodeAcademiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;

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
    @GetMapping
    public ResponseEntity<?> list(PaginationCriteria criteria) {
        int rawPage = criteria.page() != null ? criteria.page() : 1;
        int page = Math.max(rawPage - 1, 0);
        int size = criteria.size() != null ? criteria.size() : 10;
        String filter = criteria.filter() != null ? criteria.filter() : "";
        String anneeIdParam = request.getParameter("anneeScolaireId");
        Long anneeId = (anneeIdParam != null && !anneeIdParam.isBlank()) ? Long.parseLong(anneeIdParam) : null;
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
                periodeAcademiqueService.searchFiltered(anneeId, filter, pageable), request.getRequestURI()));
    }

    @Override
    protected PeriodeAcademiqueResponse doCreate(PeriodeAcademiqueRequest form) {
        PeriodeAcademique entity = new PeriodeAcademique();
        String code = form.getCode();
        if (code == null || code.isBlank()) {
            code = "PA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        }
        entity.setCode(code);
        entity.setLibelle(form.getLibelle());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setDateDebut(form.getDateDebut());
        entity.setDateFin(form.getDateFin());
        entity.setVerrouille(form.getVerrouille());

        periodeAcademiqueService.verifierChevauchement(
                form.getAnneeScolaireId(), form.getDateDebut(), form.getDateFin(), null);
        PeriodeAcademique saved = periodeAcademiqueService.create(entity);
        return periodeAcademiqueService.toResponse(saved.getId());
    }

    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        return periodeAcademiqueService.delete(uuids);
    }

    @Override
    protected PeriodeAcademiqueResponse doUpdate(String uuid, PeriodeAcademiqueRequest form) {
        PeriodeAcademique entity = periodeAcademiqueService.findByUuid(uuid);
        entity.setLibelle(form.getLibelle());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setDateDebut(form.getDateDebut());
        entity.setDateFin(form.getDateFin());
        entity.setVerrouille(form.getVerrouille());

        periodeAcademiqueService.verifierChevauchement(
                form.getAnneeScolaireId(), form.getDateDebut(), form.getDateFin(), entity.getId());
        periodeAcademiqueService.update(entity);
        return periodeAcademiqueService.toResponse(entity.getId());
    }
}
