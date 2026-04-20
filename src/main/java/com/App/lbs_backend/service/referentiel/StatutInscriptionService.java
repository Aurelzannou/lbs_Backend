package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.StatutInscriptionResponse;
import com.App.lbs_backend.entity.StatutInscription;
import com.App.lbs_backend.mapper.StatutInscriptionMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class StatutInscriptionService extends AbstractBaseService<StatutInscription, StatutInscriptionResponse> {
    
    private final StatutInscriptionRepository statutInscriptionRepository;
    private final StatutInscriptionMapper statutInscriptionMapper;

    public StatutInscriptionService(StatutInscriptionRepository statutInscriptionRepository, StatutInscriptionMapper statutInscriptionMapper) {
        super(StatutInscription.class);
        this.statutInscriptionRepository = statutInscriptionRepository;
        this.statutInscriptionMapper = statutInscriptionMapper;
    }

    @Override
    public BaseRepository<StatutInscription> repository() {
        return statutInscriptionRepository;
    }

    @Override
    public Mapper<StatutInscription, StatutInscriptionResponse> mapper() {
        return statutInscriptionMapper;
    }
}
