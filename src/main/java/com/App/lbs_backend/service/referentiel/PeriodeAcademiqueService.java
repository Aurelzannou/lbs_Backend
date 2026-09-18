package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.http.response.PageResponse;
import com.App.lbs_backend.dto.response.PeriodeAcademiqueResponse;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.mapper.PeriodeAcademiqueMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.PeriodeAcademiqueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

    /** Liste paginée des périodes, filtrable par année scolaire (utilisé par l'écran de gestion
        des périodes, qui affiche par défaut l'année scolaire active). */
    @Transactional(readOnly = true)
    public PageResponse<?> searchFiltered(Long anneeScolaireId, String filter, Pageable pageable) {
        Page<PeriodeAcademique> page = periodeAcademiqueRepository.searchFiltered(anneeScolaireId, filter, pageable);
        return paginateResponse(page);
    }

    /** Empêche deux périodes de la même année scolaire de se chevaucher dans le temps (ex :
        impossible de créer un "2e Trimestre" qui commence avant la fin du "1er Trimestre"). */
    public void verifierChevauchement(Long anneeScolaireId, LocalDate dateDebut, LocalDate dateFin, Long excludeId) {
        if (anneeScolaireId == null || dateDebut == null || dateFin == null) return;
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début.");
        }
        var chevauchantes = periodeAcademiqueRepository.findChevauchantes(anneeScolaireId, dateDebut, dateFin, excludeId);
        if (!chevauchantes.isEmpty()) {
            String autres = chevauchantes.stream().map(PeriodeAcademique::getLibelle)
                    .reduce((a, b) -> a + ", " + b).orElse("");
            throw new IllegalArgumentException(
                    "Cette période chevauche une période existante sur cette année scolaire : " + autres);
        }
    }
}
