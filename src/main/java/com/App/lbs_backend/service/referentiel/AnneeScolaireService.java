package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.AnneeScolaireResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.mapper.AnneeScolaireMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import org.springframework.stereotype.Service;

@Service
public class AnneeScolaireService extends AbstractBaseService<AnneeScolaire, AnneeScolaireResponse> {
    
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final AnneeScolaireMapper anneeScolaireMapper;

    public AnneeScolaireService(AnneeScolaireRepository anneeScolaireRepository, AnneeScolaireMapper anneeScolaireMapper) {
        super(AnneeScolaire.class);
        this.anneeScolaireRepository = anneeScolaireRepository;
        this.anneeScolaireMapper = anneeScolaireMapper;
    }

    @Override
    public BaseRepository<AnneeScolaire> repository() {
        return anneeScolaireRepository;
    }

    @Override
    public Mapper<AnneeScolaire, AnneeScolaireResponse> mapper() {
        return anneeScolaireMapper;
    }
}
