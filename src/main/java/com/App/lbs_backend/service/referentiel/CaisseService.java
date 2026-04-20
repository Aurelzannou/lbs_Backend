package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.CaisseResponse;
import com.App.lbs_backend.entity.Caisse;
import com.App.lbs_backend.mapper.CaisseMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.CaisseRepository;
import org.springframework.stereotype.Service;

@Service
public class CaisseService extends AbstractBaseService<Caisse, CaisseResponse> {
    
    private final CaisseRepository caisseRepository;
    private final CaisseMapper caisseMapper;

    public CaisseService(CaisseRepository caisseRepository, CaisseMapper caisseMapper) {
        super(Caisse.class);
        this.caisseRepository = caisseRepository;
        this.caisseMapper = caisseMapper;
    }

    @Override
    public BaseRepository<Caisse> repository() {
        return caisseRepository;
    }

    @Override
    public Mapper<Caisse, CaisseResponse> mapper() {
        return caisseMapper;
    }
}
