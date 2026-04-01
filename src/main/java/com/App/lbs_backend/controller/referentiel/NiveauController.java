package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.NiveauRequest;
import com.App.lbs_backend.dto.response.NiveauResponse;
import com.App.lbs_backend.entity.Niveau;
import com.App.lbs_backend.service.referentiel.NiveauService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Super contrôleur pour la gestion des Niveaux.
 * Il récupère automatiquement : getAll, getPaginated, Filter, FindOne, delete, massDelete!
 */
@RestController
@RequestMapping("/api/niveaux")
@RequiredArgsConstructor
public class NiveauController extends MasterController<Niveau, NiveauResponse, NiveauRequest> {

    private final NiveauService niveauService;

    @Override
    protected AbstractBaseService<Niveau, NiveauResponse> service() {
        return niveauService;
    }

    @Override
    protected NiveauResponse doCreate(NiveauRequest form) {
        Niveau niveau = new Niveau();
        niveau.setCode(form.code());
        niveau.setLibelle(form.libelle());
        
        // La méthode saveAndFlush renvoie l'entité avec son ID généré
        Niveau saved = niveauService.create(niveau);
        return niveauService.toResponse(saved.getId());
    }

    @Override
    protected NiveauResponse doUpdate(String uuid, NiveauRequest form) {
        // FindByUuid est fourni par AbstractBaseService
        Niveau niveau = niveauService.findByUuid(uuid);
        niveau.setCode(form.code());
        niveau.setLibelle(form.libelle());
        
        niveauService.update(niveau);
        return niveauService.toResponse(niveau.getId());
    }
}
