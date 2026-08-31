package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.core.http.request.UuidsRequest;
import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.DepenseScolaireRequest;
import com.App.lbs_backend.dto.response.DepenseScolaireResponse;
import com.App.lbs_backend.entity.DepenseScolaire;
import com.App.lbs_backend.service.referentiel.DepenseScolaireService;
import com.App.lbs_backend.service.scolarite.CaisseMouvementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/depenses-scolaires")
@RequiredArgsConstructor
public class DepenseScolaireController extends MasterController<DepenseScolaire, DepenseScolaireResponse, DepenseScolaireRequest> {

    private final DepenseScolaireService depenseScolaireService;
    private final CaisseMouvementService caisseMouvementService;

    @Override
    protected AbstractBaseService<DepenseScolaire, DepenseScolaireResponse> service() {
        return depenseScolaireService;
    }

    @Override
    protected DepenseScolaireResponse doCreate(DepenseScolaireRequest form) {
        DepenseScolaire entity = new DepenseScolaire();
        entity.setReference(form.getReference());
        entity.setCaisseId(form.getCaisseId());
        entity.setCategorieDepenseId(form.getCategorieDepenseId());
        entity.setMontant(form.getMontant());
        entity.setDateDepense(form.getDateDepense());
        entity.setMotif(form.getMotif());
        entity.setUtilisateurId(form.getUtilisateurId());
        entity.setAnnule(false);
        entity.setCode("DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        DepenseScolaire saved = depenseScolaireService.create(entity);
        caisseMouvementService.enregistrerMouvement(
                saved.getCaisseId(), CaisseMouvementService.SORTIE, saved.getMontant(),
                "DEPENSE", saved.getId(), saved.getMotif());

        return depenseScolaireService.toResponse(saved.getId());
    }

    @Override
    protected DepenseScolaireResponse doUpdate(String uuid, DepenseScolaireRequest form) {
        throw new IllegalArgumentException("Une dépense ne peut pas être modifiée une fois enregistrée — utilisez l'annulation.");
    }

    @Override
    protected boolean doDelete(UuidsRequest uuids) {
        throw new IllegalArgumentException("Une dépense ne peut pas être supprimée — utilisez l'annulation.");
    }

    @PutMapping("/{uuid}/annuler")
    public ResponseEntity<?> annuler(@PathVariable String uuid) {
        DepenseScolaireResponse dto = depenseScolaireService.annuler(uuid);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Dépense annulée", dto, request.getRequestURI()));
    }
}
