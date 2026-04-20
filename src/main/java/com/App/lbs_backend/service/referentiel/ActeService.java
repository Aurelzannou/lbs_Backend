package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.ActeResponse;
import com.App.lbs_backend.entity.Acte;
import com.App.lbs_backend.mapper.ActeMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ActeRepository;
import org.springframework.stereotype.Service;

@Service
public class ActeService extends AbstractBaseService<Acte, ActeResponse> {
    
    private final ActeRepository acteRepository;
    private final ActeMapper acteMapper;

    public ActeService(ActeRepository acteRepository, ActeMapper acteMapper) {
        super(Acte.class);
        this.acteRepository = acteRepository;
        this.acteMapper = acteMapper;
    }

    @Override
    public BaseRepository<Acte> repository() {
        return acteRepository;
    }

    @Override
    public Mapper<Acte, ActeResponse> mapper() {
        return acteMapper;
    }
}
