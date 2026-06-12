package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.mapper.DossierEleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service CRUD pour les dossiers d'inscription.
 * Responsabilité unique : opérations de base sur l'entité DossierEleve.
 * La logique métier (accepter, refuser, notifier) est dans ValidationService.
 * L'orchestration de la soumission est dans InscriptionService.
 */
@Service
public class DossierEleveService extends AbstractBaseService<DossierEleve, DossierEleveResponse> {

    private final DossierEleveRepository      dossierEleveRepository;
    private final DossierEleveMapper          dossierEleveMapper;
    private final StatutInscriptionRepository statutRepository;

    public DossierEleveService(DossierEleveRepository dossierEleveRepository,
                               DossierEleveMapper dossierEleveMapper,
                               StatutInscriptionRepository statutRepository) {
        super(DossierEleve.class);
        this.dossierEleveRepository = dossierEleveRepository;
        this.dossierEleveMapper     = dossierEleveMapper;
        this.statutRepository       = statutRepository;
    }

    @Override
    public BaseRepository<DossierEleve> repository() { return dossierEleveRepository; }

    @Override
    public Mapper<DossierEleve, DossierEleveResponse> mapper() { return dossierEleveMapper; }

    /** Auto-set du statut DEPOSE lors de la création d'un dossier. */
    public void setStatutDepose(DossierEleve dossier) {
        statutRepository.findByCode("DEPOSE")
                .ifPresent(s -> dossier.setStatutId(s.getId()));
    }

    /** Retourne les dossiers d'un tuteur. */
    public List<DossierEleveResponse> getByTuteurId(Long tuteurId) {
        return dossierEleveRepository.findByTuteurId(tuteurId).stream()
                .map(d -> mapper().toResponse(d))
                .collect(Collectors.toList());
    }
}
