package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.FraisScolaireResponse;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.mapper.FraisScolaireMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.FraisScolaireRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FraisScolaireService extends AbstractBaseService<FraisScolaire, FraisScolaireResponse> {
    
    private final FraisScolaireRepository fraisScolaireRepository;
    private final FraisScolaireMapper fraisScolaireMapper;

    public FraisScolaireService(FraisScolaireRepository fraisScolaireRepository, FraisScolaireMapper fraisScolaireMapper) {
        super(FraisScolaire.class);
        this.fraisScolaireRepository = fraisScolaireRepository;
        this.fraisScolaireMapper = fraisScolaireMapper;
    }

    @Override
    public BaseRepository<FraisScolaire> repository() {
        return fraisScolaireRepository;
    }

    @Override
    public Mapper<FraisScolaire, FraisScolaireResponse> mapper() {
        return fraisScolaireMapper;
    }

    public List<FraisScolaireResponse> findByClasseAndAnnee(Long classeId, Long anneeScolaireId) {
        return fraisScolaireRepository
                .findByClasseIdAndAnneeScolaireId(classeId, anneeScolaireId)
                .stream()
                .map(fraisScolaireMapper::toResponse)
                .collect(Collectors.toList());
    }
}
