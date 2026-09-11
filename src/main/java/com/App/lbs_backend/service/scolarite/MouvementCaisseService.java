package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.MouvementCaisseResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.entity.MouvementCaisse;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.MouvementCaisseMapper;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.MouvementCaisseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MouvementCaisseService extends AbstractBaseService<MouvementCaisse, MouvementCaisseResponse> {

    private final MouvementCaisseRepository mouvementCaisseRepository;
    private final MouvementCaisseMapper mouvementCaisseMapper;
    private final AnneeScolaireRepository anneeScolaireRepository;

    public MouvementCaisseService(MouvementCaisseRepository mouvementCaisseRepository, MouvementCaisseMapper mouvementCaisseMapper,
                                  AnneeScolaireRepository anneeScolaireRepository) {
        super(MouvementCaisse.class);
        this.mouvementCaisseRepository = mouvementCaisseRepository;
        this.mouvementCaisseMapper = mouvementCaisseMapper;
        this.anneeScolaireRepository = anneeScolaireRepository;
    }

    @Override
    public BaseRepository<MouvementCaisse> repository() {
        return mouvementCaisseRepository;
    }

    @Override
    public Mapper<MouvementCaisse, MouvementCaisseResponse> mapper() {
        return mouvementCaisseMapper;
    }

    public java.util.List<MouvementCaisseResponse> listerParCaisse(Long caisseId) {
        return listerParCaisse(caisseId, null);
    }

    /**
     * Journal d'une caisse, restreignable à une année scolaire via son intervalle de dates
     * (dateDebut/dateFin) — les mouvements n'étant pas eux-mêmes rattachés à une année (dépenses
     * comprises), c'est le seul critère commun aux paiements et aux dépenses.
     */
    public java.util.List<MouvementCaisseResponse> listerParCaisse(Long caisseId, Long anneeScolaireId) {
        java.util.List<MouvementCaisse> mouvements;
        if (anneeScolaireId == null) {
            mouvements = mouvementCaisseRepository.findByCaisseIdOrderByDateMouvementDesc(caisseId);
        } else {
            AnneeScolaire annee = anneeScolaireRepository.findById(anneeScolaireId).orElse(null);
            LocalDateTime debut = annee != null && annee.getDateDebut() != null
                    ? annee.getDateDebut().atStartOfDay() : null;
            LocalDateTime fin = annee != null && annee.getDateFin() != null
                    ? annee.getDateFin().plusDays(1).atStartOfDay() : null;
            mouvements = mouvementCaisseRepository.findByCaisseIdAndPeriode(caisseId, debut, fin);
        }
        return mouvements.stream().map(mouvementCaisseMapper::toResponse).toList();
    }
}
