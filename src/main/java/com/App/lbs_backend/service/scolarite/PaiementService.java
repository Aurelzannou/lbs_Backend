package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.response.PaiementResponse;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.PaiementMapper;
import com.App.lbs_backend.repository.PaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaiementService extends AbstractBaseService<Paiement, PaiementResponse> {

    private final PaiementRepository paiementRepository;
    private final PaiementMapper paiementMapper;
    private final CaisseMouvementService caisseMouvementService;

    public PaiementService(PaiementRepository paiementRepository, PaiementMapper paiementMapper,
                            CaisseMouvementService caisseMouvementService) {
        super(Paiement.class);
        this.paiementRepository = paiementRepository;
        this.paiementMapper = paiementMapper;
        this.caisseMouvementService = caisseMouvementService;
    }

    @Override
    public BaseRepository<Paiement> repository() {
        return paiementRepository;
    }

    @Override
    public Mapper<Paiement, PaiementResponse> mapper() {
        return paiementMapper;
    }

    @Transactional
    public PaiementResponse annuler(String uuid) {
        Paiement entity = findByUuid(uuid);
        if (!"SUCCES".equals(entity.getStatutTransaction())) {
            throw new IllegalArgumentException("Seul un paiement réussi peut être annulé.");
        }
        caisseMouvementService.enregistrerMouvement(
                entity.getCaisseId(), CaisseMouvementService.SORTIE, entity.getMontant(),
                "PAIEMENT", entity.getId(), "Annulation paiement " + entity.getCode());
        entity.setStatutTransaction("ANNULE");
        update(entity);
        return toResponse(entity.getId());
    }
}
