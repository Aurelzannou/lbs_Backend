package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.MatiereResponse;
import com.App.lbs_backend.entity.Matiere;
import com.App.lbs_backend.mapper.MatiereMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.MatiereRepository;
import org.springframework.stereotype.Service;

@Service
public class MatiereService extends AbstractBaseService<Matiere, MatiereResponse> {
    
    private final MatiereRepository matiereRepository;
    private final MatiereMapper matiereMapper;

    public MatiereService(MatiereRepository matiereRepository, MatiereMapper matiereMapper) {
        super(Matiere.class);
        this.matiereRepository = matiereRepository;
        this.matiereMapper = matiereMapper;
    }

    @Override
    public BaseRepository<Matiere> repository() {
        return matiereRepository;
    }

    @Override
    public Mapper<Matiere, MatiereResponse> mapper() {
        return matiereMapper;
    }
}
