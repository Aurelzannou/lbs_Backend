package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.PeriodeAcademiqueResponse;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.mapper.PeriodeAcademiqueMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.PeriodeAcademiqueRepository;
import org.springframework.stereotype.Service;

@Service
public class PeriodeAcademiqueService extends AbstractBaseService<PeriodeAcademique, PeriodeAcademiqueResponse> {
    
    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final PeriodeAcademiqueMapper periodeAcademiqueMapper;

    public PeriodeAcademiqueService(PeriodeAcademiqueRepository periodeAcademiqueRepository, PeriodeAcademiqueMapper periodeAcademiqueMapper) {
        super(PeriodeAcademique.class);
        this.periodeAcademiqueRepository = periodeAcademiqueRepository;
        this.periodeAcademiqueMapper = periodeAcademiqueMapper;
    }

    @Override
    public BaseRepository<PeriodeAcademique> repository() {
        return periodeAcademiqueRepository;
    }

    @Override
    public Mapper<PeriodeAcademique, PeriodeAcademiqueResponse> mapper() {
        return periodeAcademiqueMapper;
    }
}
