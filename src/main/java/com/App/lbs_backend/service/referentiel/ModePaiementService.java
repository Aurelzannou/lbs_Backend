package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.ModePaiementResponse;
import com.App.lbs_backend.entity.ModePaiement;
import com.App.lbs_backend.mapper.ModePaiementMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ModePaiementRepository;
import org.springframework.stereotype.Service;

@Service
public class ModePaiementService extends AbstractBaseService<ModePaiement, ModePaiementResponse> {
    
    private final ModePaiementRepository modePaiementRepository;
    private final ModePaiementMapper modePaiementMapper;

    public ModePaiementService(ModePaiementRepository modePaiementRepository, ModePaiementMapper modePaiementMapper) {
        super(ModePaiement.class);
        this.modePaiementRepository = modePaiementRepository;
        this.modePaiementMapper = modePaiementMapper;
    }

    @Override
    public BaseRepository<ModePaiement> repository() {
        return modePaiementRepository;
    }

    @Override
    public Mapper<ModePaiement, ModePaiementResponse> mapper() {
        return modePaiementMapper;
    }
}
