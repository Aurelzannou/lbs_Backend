package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.NiveauResponse;
import com.App.lbs_backend.entity.Niveau;
import com.App.lbs_backend.mapper.NiveauMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.NiveauRepository;
import org.springframework.stereotype.Service;

@Service
public class NiveauService extends AbstractBaseService<Niveau, NiveauResponse> {
    
    // Spring boot va injecter ces dépendances automatiquement
    private final NiveauRepository niveauRepository;
    private final NiveauMapper niveauMapper;

    // Construction requise par Java pour AbstractBaseService
    public NiveauService(NiveauRepository niveauRepository, NiveauMapper niveauMapper) {
        super(Niveau.class);
        this.niveauRepository = niveauRepository;
        this.niveauMapper = niveauMapper;
    }

    @Override
    protected BaseRepository<Niveau> repository() {
        return niveauRepository;
    }

    @Override
    protected Mapper<Niveau, NiveauResponse> mapper() {
        return niveauMapper;
    }
}
