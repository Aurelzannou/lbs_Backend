package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Paiement;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends BaseRepository<Paiement> {

    List<Paiement> findByDossierEleveIdAndFraisScolaireIdAndStatutTransaction(
            Long dossierEleveId, Long fraisScolaireId, String statutTransaction);

    /** Retrouve un paiement par la référence externe (id de transaction FedaPay). */
    Optional<Paiement> findByReference(String reference);

    List<Paiement> findByDossierEleveIdAndFraisScolaireId(Long dossierEleveId, Long fraisScolaireId);
}