package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.response.PaiementResponse;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.PaiementMapper;
import com.App.lbs_backend.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class PaiementService extends AbstractBaseService<Paiement, PaiementResponse> {

    private final PaiementRepository paiementRepository;
    private final PaiementMapper paiementMapper;

    public PaiementService(PaiementRepository paiementRepository, PaiementMapper paiementMapper) {
        super(Paiement.class);
        this.paiementRepository = paiementRepository;
        this.paiementMapper = paiementMapper;
    }

    @Override
    public BaseRepository<Paiement> repository() {
        return paiementRepository;
    }

    @Override
    public Mapper<Paiement, PaiementResponse> mapper() {
        return paiementMapper;
    }
}
