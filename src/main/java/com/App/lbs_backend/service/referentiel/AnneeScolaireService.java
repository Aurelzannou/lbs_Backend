package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.AnneeScolaireResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.mapper.AnneeScolaireMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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

    /** Deux années scolaires ne peuvent jamais se chevaucher — un jour donné appartient à une seule
        année. */
    public void verifierChevauchement(LocalDate dateDebut, LocalDate dateFin, Long excludeId) {
        if (dateDebut == null || dateFin == null) return;
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
        }
        if (!anneeScolaireRepository.findChevauchantes(dateDebut, dateFin, excludeId).isEmpty()) {
            throw new IllegalArgumentException(
                    "Cette période chevauche une autre année scolaire déjà existante.");
        }
    }

    /** Une seule année scolaire peut être active à la fois — activer celle-ci désactive
        automatiquement toutes les autres. */
    public void assurerActiviteUnique(AnneeScolaire entity) {
        if (Boolean.TRUE.equals(entity.getActif())) {
            anneeScolaireRepository.desactiverAutres(entity.getId());
        }
    }
}
