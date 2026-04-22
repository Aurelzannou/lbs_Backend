package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.mapper.DossierEleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.DossierEleveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class DossierEleveService extends AbstractBaseService<DossierEleve, DossierEleveResponse> {

    private final DossierEleveRepository dossierEleveRepository;
    private final DossierEleveMapper dossierEleveMapper;

    public DossierEleveService(DossierEleveRepository dossierEleveRepository, DossierEleveMapper dossierEleveMapper) {
        super(DossierEleve.class);
        this.dossierEleveRepository = dossierEleveRepository;
        this.dossierEleveMapper = dossierEleveMapper;
    }

    @Override
    public BaseRepository<DossierEleve> repository() {
        return dossierEleveRepository;
    }

    @Override
    public Mapper<DossierEleve, DossierEleveResponse> mapper() {
        return dossierEleveMapper;
    }
}
