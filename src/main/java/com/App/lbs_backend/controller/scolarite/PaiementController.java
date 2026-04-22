package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.PaiementRequest;
import com.App.lbs_backend.dto.response.PaiementResponse;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.service.scolarite.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementController extends MasterController<Paiement, PaiementResponse, PaiementRequest> {

    private final PaiementService paiementService;

    @Override
    protected AbstractBaseService<Paiement, PaiementResponse> service() {
        return paiementService;
    }

    @Override
    protected PaiementResponse doCreate(PaiementRequest form) {
        Paiement paiement = new Paiement();
        mapFormToEntity(form, paiement);
        Paiement saved = paiementService.create(paiement);
        return paiementService.toResponse(saved.getId());
    }

    @Override
    protected PaiementResponse doUpdate(String uuid, PaiementRequest form) {
        Paiement paiement = paiementService.findByUuid(uuid);
        mapFormToEntity(form, paiement);
        paiementService.update(paiement);
        return paiementService.toResponse(paiement.getId());
    }

    private void mapFormToEntity(PaiementRequest form, Paiement paiement) {
        paiement.setCode(form.getCode());
        paiement.setReference(form.getReference());
        paiement.setDossierEleveId(form.getDossierEleveId());
        paiement.setFraisScolaireId(form.getFraisScolaireId());
        paiement.setDatePaiement(form.getDatePaiement());
        paiement.setMontant(form.getMontant());
        paiement.setModePaiementId(form.getModePaiementId());
        paiement.setCaisseId(form.getCaisseId());
        paiement.setUtilisateurId(form.getUtilisateurId());
        paiement.setObservation(form.getObservation());
        paiement.setCanal(form.getCanal());
        paiement.setStatutTransaction(form.getStatutTransaction());
        paiement.setTelephonePaiement(form.getTelephonePaiement());
    }
}
