package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.DepenseScolaireResponse;
import com.App.lbs_backend.entity.DepenseScolaire;
import com.App.lbs_backend.mapper.DepenseScolaireMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.DepenseScolaireRepository;
import com.App.lbs_backend.service.scolarite.CaisseMouvementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepenseScolaireService extends AbstractBaseService<DepenseScolaire, DepenseScolaireResponse> {

    private final DepenseScolaireRepository depenseScolaireRepository;
    private final DepenseScolaireMapper depenseScolaireMapper;
    private final CaisseMouvementService caisseMouvementService;

    public DepenseScolaireService(DepenseScolaireRepository depenseScolaireRepository,
                                   DepenseScolaireMapper depenseScolaireMapper,
                                   CaisseMouvementService caisseMouvementService) {
        super(DepenseScolaire.class);
        this.depenseScolaireRepository = depenseScolaireRepository;
        this.depenseScolaireMapper = depenseScolaireMapper;
        this.caisseMouvementService = caisseMouvementService;
    }

    @Override
    public BaseRepository<DepenseScolaire> repository() {
        return depenseScolaireRepository;
    }

    @Override
    public Mapper<DepenseScolaire, DepenseScolaireResponse> mapper() {
        return depenseScolaireMapper;
    }

    @Transactional
    public DepenseScolaireResponse annuler(String uuid) {
        DepenseScolaire entity = findByUuid(uuid);
        if (Boolean.TRUE.equals(entity.getAnnule())) {
            throw new IllegalArgumentException("Cette dépense a déjà été annulée.");
        }
        caisseMouvementService.enregistrerMouvement(
                entity.getCaisseId(), CaisseMouvementService.ENTREE, entity.getMontant(),
                "DEPENSE", entity.getId(), "Annulation dépense " + entity.getCode());
        entity.setAnnule(true);
        update(entity);
        return toResponse(entity.getId());
    }
}
