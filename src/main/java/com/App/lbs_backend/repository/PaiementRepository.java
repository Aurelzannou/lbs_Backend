package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Paiement;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends BaseRepository<Paiement> {

    List<Paiement> findByDossierEleveIdAndFraisScolaireIdAndStatutTransaction(
            Long dossierEleveId, Long fraisScolaireId, String statutTransaction);
}