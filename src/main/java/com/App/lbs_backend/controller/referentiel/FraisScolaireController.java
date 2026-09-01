package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.FraisScolaireRequest;
import com.App.lbs_backend.dto.response.FraisScolaireResponse;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.service.referentiel.FraisScolaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/frais-scolaires")
@RequiredArgsConstructor
public class FraisScolaireController extends MasterController<FraisScolaire, FraisScolaireResponse, FraisScolaireRequest> {

    private final FraisScolaireService fraisScolaireService;

    @Override
    protected AbstractBaseService<FraisScolaire, FraisScolaireResponse> service() {
        return fraisScolaireService;
    }

    @Override
    protected FraisScolaireResponse doCreate(FraisScolaireRequest form) {
        FraisScolaire entity = new FraisScolaire();
        entity.setCode(form.getCode());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setClasseId(form.getClasseId());
        entity.setTypeFraisId(form.getTypeFraisId());
        entity.setMontant(form.getMontant());
        entity.setActif(form.getActif());
        
        FraisScolaire saved = fraisScolaireService.create(entity);
        return fraisScolaireService.toResponse(saved.getId());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam Long classeId,
            @RequestParam Long anneeScolaireId) {
        // Enveloppe ApiResponse comme le reste de l'API : le front (ApiService) déballe
        // systématiquement `.data`, un tableau nu renvoyait donc `undefined` côté client.
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
                fraisScolaireService.findByClasseAndAnnee(classeId, anneeScolaireId),
                request.getRequestURI()));
    }

    @Override
    protected FraisScolaireResponse doUpdate(String uuid, FraisScolaireRequest form) {
        FraisScolaire entity = fraisScolaireService.findByUuid(uuid);
        entity.setCode(form.getCode());
        entity.setAnneeScolaireId(form.getAnneeScolaireId());
        entity.setClasseId(form.getClasseId());
        entity.setTypeFraisId(form.getTypeFraisId());
        entity.setMontant(form.getMontant());
        entity.setActif(form.getActif());
        
        fraisScolaireService.update(entity);
        return fraisScolaireService.toResponse(entity.getId());
    }
}
