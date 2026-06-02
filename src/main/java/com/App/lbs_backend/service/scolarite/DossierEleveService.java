package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.BaseRepository;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.StatutInscription;
import com.App.lbs_backend.mapper.DossierEleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DossierEleveService extends AbstractBaseService<DossierEleve, DossierEleveResponse> {

    private final DossierEleveRepository dossierEleveRepository;
    private final DossierEleveMapper dossierEleveMapper;
    private final StatutInscriptionRepository statutRepository;

    public DossierEleveService(DossierEleveRepository dossierEleveRepository,
                               DossierEleveMapper dossierEleveMapper,
                               StatutInscriptionRepository statutRepository) {
        super(DossierEleve.class);
        this.dossierEleveRepository = dossierEleveRepository;
        this.dossierEleveMapper = dossierEleveMapper;
        this.statutRepository = statutRepository;
    }

    @Override
    public BaseRepository<DossierEleve> repository() {
        return dossierEleveRepository;
    }

    @Override
    public Mapper<DossierEleve, DossierEleveResponse> mapper() {
        return dossierEleveMapper;
    }

    @Transactional
    public DossierEleveResponse changerStatut(String uuid, String statutCode) {
        DossierEleve dossier = findByUuid(uuid);
        StatutInscription statut = statutRepository.findByCode(statutCode)
                .orElseThrow(() -> new RuntimeException("Statut non trouvé : " + statutCode));
        dossier.setStatutId(statut.getId());
        update(dossier);
        return toResponse(dossier.getId());
    }

    public List<DossierEleveResponse> getByTuteurId(Long tuteurId) {
        return dossierEleveRepository.findByTuteurId(tuteurId).stream()
                .map(d -> mapper().toResponse(d))
                .collect(Collectors.toList());
    }
}
