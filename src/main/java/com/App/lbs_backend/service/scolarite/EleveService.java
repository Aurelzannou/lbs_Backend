package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.EleveResponse;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.mapper.EleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.EleveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class EleveService extends AbstractBaseService<Eleve, EleveResponse> {

    private final EleveRepository eleveRepository;
    private final EleveMapper eleveMapper;

    public EleveService(EleveRepository eleveRepository, EleveMapper eleveMapper) {
        super(Eleve.class);
        this.eleveRepository = eleveRepository;
        this.eleveMapper = eleveMapper;
    }

    @Override
    public BaseRepository<Eleve> repository() {
        return eleveRepository;
    }

    @Override
    public Mapper<Eleve, EleveResponse> mapper() {
        return eleveMapper;
    }
}
