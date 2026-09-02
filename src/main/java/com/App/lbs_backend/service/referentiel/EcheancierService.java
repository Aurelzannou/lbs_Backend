package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.EcheancierResponse;
import com.App.lbs_backend.entity.Echeancier;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.mapper.EcheancierMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.EcheancierRepository;
import com.App.lbs_backend.repository.FraisScolaireRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class EcheancierService extends AbstractBaseService<Echeancier, EcheancierResponse> {

    private final EcheancierRepository echeancierRepository;
    private final EcheancierMapper echeancierMapper;
    private final FraisScolaireRepository fraisScolaireRepository;

    public EcheancierService(EcheancierRepository echeancierRepository, EcheancierMapper echeancierMapper,
                             FraisScolaireRepository fraisScolaireRepository) {
        super(Echeancier.class);
        this.echeancierRepository = echeancierRepository;
        this.echeancierMapper = echeancierMapper;
        this.fraisScolaireRepository = fraisScolaireRepository;
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

    /**
     * Contrôle métier : la somme des tranches d'un échéancier ne peut pas dépasser le montant
     * du frais scolaire. Si la contribution est de 100 000 FCFA, les tranches doivent se
     * répartir dans cette limite.
     *
     * @param uuidEnCours uuid de la tranche en cours de modification (exclue du cumul), ou null
     *                    lors d'une création.
     */
    public void verifierTotalTranches(Long fraisScolaireId, Double montantTranche, String uuidEnCours) {
        if (fraisScolaireId == null || montantTranche == null) return;

        FraisScolaire frais = fraisScolaireRepository.findById(fraisScolaireId).orElse(null);
        if (frais == null || frais.getMontant() == null || frais.getMontant() <= 0) return;

        double dejaReparti = echeancierRepository.findByFraisScolaireIdOrderByNumeroAsc(fraisScolaireId).stream()
                .filter(e -> uuidEnCours == null || !uuidEnCours.equals(e.getUuid()))
                .mapToDouble(e -> e.getMontant() != null ? e.getMontant() : 0.0)
                .sum();

        double nouveauTotal = dejaReparti + montantTranche;
        if (nouveauTotal > frais.getMontant() + 0.01) {
            double reste = Math.max(0, frais.getMontant() - dejaReparti);
            throw new IllegalArgumentException(String.format(Locale.FRANCE,
                    "Le total des tranches (%,.0f FCFA) dépasserait le montant du frais (%,.0f FCFA). "
                    + "Il reste %,.0f FCFA à répartir.",
                    nouveauTotal, frais.getMontant(), reste));
        }
    }
}
