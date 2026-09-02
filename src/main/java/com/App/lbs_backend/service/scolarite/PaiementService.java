package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.response.PaiementResponse;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.PaiementMapper;
import com.App.lbs_backend.repository.FraisScolaireRepository;
import com.App.lbs_backend.repository.PaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class PaiementService extends AbstractBaseService<Paiement, PaiementResponse> {

    private final PaiementRepository paiementRepository;
    private final PaiementMapper paiementMapper;
    private final CaisseMouvementService caisseMouvementService;
    private final FraisScolaireRepository fraisScolaireRepository;

    public PaiementService(PaiementRepository paiementRepository, PaiementMapper paiementMapper,
                            CaisseMouvementService caisseMouvementService,
                            FraisScolaireRepository fraisScolaireRepository) {
        super(Paiement.class);
        this.paiementRepository = paiementRepository;
        this.paiementMapper = paiementMapper;
        this.caisseMouvementService = caisseMouvementService;
        this.fraisScolaireRepository = fraisScolaireRepository;
    }

    /**
     * Interdit d'encaisser plus que le reste à payer d'un frais : la somme des paiements SUCCES
     * (existants) + le nouveau montant ne peut pas dépasser le montant du frais.
     */
    public void verifierPasDeSurpaiement(Long dossierEleveId, Long fraisScolaireId, Double montant) {
        if (dossierEleveId == null || fraisScolaireId == null || montant == null || montant <= 0) return;

        FraisScolaire frais = fraisScolaireRepository.findById(fraisScolaireId).orElse(null);
        if (frais == null || frais.getMontant() == null || frais.getMontant() <= 0) return;

        double dejaPaye = paiementRepository
                .findByDossierEleveIdAndFraisScolaireIdAndStatutTransaction(dossierEleveId, fraisScolaireId, "SUCCES")
                .stream().mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0).sum();

        double reste = frais.getMontant() - dejaPaye;
        if (montant > reste + 0.01) {
            String msg = reste <= 0
                    ? "Ce frais est déjà entièrement réglé."
                    : String.format(Locale.FRANCE,
                        "Le montant saisi (%,.0f FCFA) dépasse le reste à payer (%,.0f FCFA).", montant, reste);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public BaseRepository<Paiement> repository() {
        return paiementRepository;
    }

    @Override
    public Mapper<Paiement, PaiementResponse> mapper() {
        return paiementMapper;
    }

    @Transactional
    public PaiementResponse annuler(String uuid) {
        Paiement entity = findByUuid(uuid);
        if (!"SUCCES".equals(entity.getStatutTransaction())) {
            throw new IllegalArgumentException("Seul un paiement réussi peut être annulé.");
        }
        caisseMouvementService.enregistrerMouvement(
                entity.getCaisseId(), CaisseMouvementService.SORTIE, entity.getMontant(),
                "PAIEMENT", entity.getId(), "Annulation paiement " + entity.getCode());
        entity.setStatutTransaction("ANNULE");
        update(entity);
        return toResponse(entity.getId());
    }
}
