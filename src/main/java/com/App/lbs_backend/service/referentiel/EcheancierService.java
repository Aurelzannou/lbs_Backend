package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.EcheancierResponse;
import com.App.lbs_backend.entity.Echeancier;
import com.App.lbs_backend.mapper.EcheancierMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.EcheancierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EcheancierService extends AbstractBaseService<Echeancier, EcheancierResponse> {

    private final EcheancierRepository echeancierRepository;
    private final EcheancierMapper echeancierMapper;

    public EcheancierService(EcheancierRepository echeancierRepository, EcheancierMapper echeancierMapper) {
        super(Echeancier.class);
        this.echeancierRepository = echeancierRepository;
        this.echeancierMapper = echeancierMapper;
    }

    @Override
    public BaseRepository<Echeancier> repository() {
        return echeancierRepository;
    }

    @Override
    public Mapper<Echeancier, EcheancierResponse> mapper() {
        return echeancierMapper;
    }

    public List<EcheancierResponse> listerParFraisScolaire(Long fraisScolaireId) {
        return echeancierRepository.findByFraisScolaireIdOrderByNumeroAsc(fraisScolaireId).stream()
                .map(echeancierMapper::toResponse)
                .toList();
    }
}
