package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.entity.Caisse;
import com.App.lbs_backend.entity.MouvementCaisse;
import com.App.lbs_backend.repository.CaisseRepository;
import com.App.lbs_backend.repository.MouvementCaisseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Point d'entrée unique pour toute écriture affectant le solde d'une Caisse. Chaque paiement,
 * dépense ou annulation passe par ici plutôt que d'éditer Caisse.solde directement, pour garantir
 * que le journal MouvementCaisse reste la trace fidèle de tout ce qui a fait bouger le solde.
 */
@Service
@RequiredArgsConstructor
public class CaisseMouvementService {

    public static final String ENTREE = "ENTREE";
    public static final String SORTIE = "SORTIE";

    private final CaisseRepository caisseRepository;
    private final MouvementCaisseRepository mouvementCaisseRepository;

    @Transactional
    public MouvementCaisse enregistrerMouvement(Long caisseId, String typeMouvement, Double montant,
                                                 String source, Long sourceId, String description) {
        Caisse caisse = caisseRepository.findById(caisseId)
                .orElseThrow(() -> new IllegalArgumentException("Caisse introuvable"));

        double soldeActuel = caisse.getSolde() != null ? caisse.getSolde() : 0.0;
        double nouveauSolde = ENTREE.equals(typeMouvement) ? soldeActuel + montant : soldeActuel - montant;
        caisse.setSolde(nouveauSolde);
        caisseRepository.saveAndFlush(caisse);

        MouvementCaisse mouvement = new MouvementCaisse();
        mouvement.setCaisseId(caisseId);
        mouvement.setTypeMouvement(typeMouvement);
        mouvement.setMontant(montant);
        mouvement.setDateMouvement(LocalDateTime.now());
        mouvement.setSource(source);
        mouvement.setSourceId(sourceId);
        mouvement.setSoldeApres(nouveauSolde);
        mouvement.setDescription(description);
        mouvement.setCode("MVT-" + java.util.UUID.randomUUID().toString().substring(0, 8));

        return mouvementCaisseRepository.saveAndFlush(mouvement);
    }
}
