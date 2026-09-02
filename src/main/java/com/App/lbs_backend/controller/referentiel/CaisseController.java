package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.CaisseRequest;
import com.App.lbs_backend.dto.response.CaisseResponse;
import com.App.lbs_backend.entity.Caisse;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.service.referentiel.CaisseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caisses")
@RequiredArgsConstructor
public class CaisseController extends MasterController<Caisse, CaisseResponse, CaisseRequest> {

    private final CaisseService caisseService;

    @Override
    protected AbstractBaseService<Caisse, CaisseResponse> service() {
        return caisseService;
    }

    /** Caisse de l'utilisateur connecté (pour pré-sélection automatique sur l'écran d'encaissement).
        Renvoie data = null s'il n'a aucune caisse rattachée. */
    @GetMapping("/ma-caisse")
    public ResponseEntity<?> maCaisse() {
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK",
                caisseService.getCaisseDeLUtilisateurConnecte(), request.getRequestURI()));
    }

    @Override
    protected CaisseResponse doCreate(CaisseRequest form) {
        caisseService.verifierUtilisateurLibre(form.getUtilisateurId(), null);

        Caisse entity = new Caisse();
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setSolde(form.getSolde());
        entity.setActif(form.getActif());
        entity.setUtilisateurId(form.getUtilisateurId());

        Caisse saved = caisseService.create(entity);
        return caisseService.toResponse(saved.getId());
    }

    @Override
    protected CaisseResponse doUpdate(String uuid, CaisseRequest form) {
        Caisse entity = caisseService.findByUuid(uuid);
        caisseService.verifierUtilisateurLibre(form.getUtilisateurId(), entity.getId());

        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setSolde(form.getSolde());
        entity.setActif(form.getActif());
        entity.setUtilisateurId(form.getUtilisateurId());

        caisseService.update(entity);
        return caisseService.toResponse(entity.getId());
    }
}
