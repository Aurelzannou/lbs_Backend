package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.PeriodeRentreeResponse;
import com.App.lbs_backend.entity.PeriodeRentree;
import com.App.lbs_backend.mapper.PeriodeRentreeMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.PeriodeRentreeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PeriodeRentreeService extends AbstractBaseService<PeriodeRentree, PeriodeRentreeResponse> {

    private final PeriodeRentreeRepository periodeRentreeRepository;
    private final PeriodeRentreeMapper     periodeRentreeMapper;

    public PeriodeRentreeService(PeriodeRentreeRepository repo, PeriodeRentreeMapper mapper) {
        super(PeriodeRentree.class);
        this.periodeRentreeRepository = repo;
        this.periodeRentreeMapper     = mapper;
    }

    @Override
    public BaseRepository<PeriodeRentree> repository() {
        return periodeRentreeRepository;
    }

    @Override
    public Mapper<PeriodeRentree, PeriodeRentreeResponse> mapper() {
        return periodeRentreeMapper;
    }

    public Optional<PeriodeRentreeResponse> getPeriodeActive(Long anneeScolaireId) {
        return periodeRentreeRepository.findPeriodeActive(anneeScolaireId, LocalDate.now())
                .map(periodeRentreeMapper::toResponse);
    }
}
