package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.PeriodeInscriptionResponse;
import com.App.lbs_backend.entity.PeriodeInscription;
import com.App.lbs_backend.mapper.PeriodeInscriptionMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.PeriodeInscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PeriodeInscriptionService extends AbstractBaseService<PeriodeInscription, PeriodeInscriptionResponse> {

    private final PeriodeInscriptionRepository periodeInscriptionRepository;
    private final PeriodeInscriptionMapper     periodeInscriptionMapper;

    public PeriodeInscriptionService(PeriodeInscriptionRepository repo, PeriodeInscriptionMapper mapper) {
        super(PeriodeInscription.class);
        this.periodeInscriptionRepository = repo;
        this.periodeInscriptionMapper     = mapper;
    }

    @Override
    public BaseRepository<PeriodeInscription> repository() {
        return periodeInscriptionRepository;
    }

    @Override
    public Mapper<PeriodeInscription, PeriodeInscriptionResponse> mapper() {
        return periodeInscriptionMapper;
    }

    public Optional<PeriodeInscriptionResponse> getPeriodeActive(Long anneeScolaireId) {
        return periodeInscriptionRepository.findPeriodeActive(anneeScolaireId, LocalDate.now())
                .map(periodeInscriptionMapper::toResponse);
    }

    public void validerPeriode(Long anneeScolaireId) {
        periodeInscriptionRepository.findPeriodeActive(anneeScolaireId, LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune période d'inscription n'est ouverte pour cette année scolaire. "
                        + "Élargissez la date de clôture de la période concernée pour enregistrer un dossier."));
    }
}
