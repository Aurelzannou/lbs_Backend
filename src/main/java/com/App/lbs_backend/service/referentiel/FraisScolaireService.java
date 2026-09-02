package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.FraisScolaireResponse;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.mapper.FraisScolaireMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.FraisScolaireRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FraisScolaireService extends AbstractBaseService<FraisScolaire, FraisScolaireResponse> {

    private final FraisScolaireRepository fraisScolaireRepository;
    private final FraisScolaireMapper fraisScolaireMapper;

    /** Code du type de frais encaissé au guichet (/comptabilite/paiements). Les autres types
        (INSCRIPTION payé en ligne, etc.) n'y apparaissent pas. */
    @Value("${app.paiement.type-frais-code:SCOLARITE}")
    private String typeFraisPaiementGuichet;

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
        return findByClasseAndAnnee(classeId, anneeScolaireId, false);
    }

    /**
     * @param paiementGuichetSeul true pour ne renvoyer que le type de frais encaissé au guichet
     *                            ({@code app.paiement.type-frais-code}, « SCOLARITE » par défaut).
     */
    public List<FraisScolaireResponse> findByClasseAndAnnee(Long classeId, Long anneeScolaireId,
                                                            boolean paiementGuichetSeul) {
        return fraisScolaireRepository
                .findByClasseIdAndAnneeScolaireId(classeId, anneeScolaireId)
                .stream()
                .map(fraisScolaireMapper::toResponse)
                .filter(r -> !paiementGuichetSeul
                        || (r.typeFrais() != null
                            && typeFraisPaiementGuichet.equalsIgnoreCase(r.typeFrais().code())))
                .collect(Collectors.toList());
    }
}
