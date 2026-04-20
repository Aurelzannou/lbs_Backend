package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.CoefficientResponse;
import com.App.lbs_backend.entity.Coefficient;
import com.App.lbs_backend.mapper.CoefficientMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.CoefficientRepository;
import org.springframework.stereotype.Service;

@Service
public class CoefficientService extends AbstractBaseService<Coefficient, CoefficientResponse> {
    
    private final CoefficientRepository coefficientRepository;
    private final CoefficientMapper coefficientMapper;

    public CoefficientService(CoefficientRepository coefficientRepository, CoefficientMapper coefficientMapper) {
        super(Coefficient.class);
        this.coefficientRepository = coefficientRepository;
        this.coefficientMapper = coefficientMapper;
    }

    @Override
    public BaseRepository<Coefficient> repository() {
        return coefficientRepository;
    }

    @Override
    public Mapper<Coefficient, CoefficientResponse> mapper() {
        return coefficientMapper;
    }
}
