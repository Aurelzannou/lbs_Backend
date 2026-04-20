package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.ProfesseurResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.mapper.ProfesseurMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ProfesseurRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfesseurService extends AbstractBaseService<Professeur, ProfesseurResponse> {
    
    private final ProfesseurRepository professeurRepository;
    private final ProfesseurMapper professeurMapper;

    public ProfesseurService(ProfesseurRepository professeurRepository, ProfesseurMapper professeurMapper) {
        super(Professeur.class);
        this.professeurRepository = professeurRepository;
        this.professeurMapper = professeurMapper;
    }

    @Override
    public BaseRepository<Professeur> repository() {
        return professeurRepository;
    }

    @Override
    public Mapper<Professeur, ProfesseurResponse> mapper() {
        return professeurMapper;
    }
}
