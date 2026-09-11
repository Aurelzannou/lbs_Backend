package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Paiement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Liste des paiements pour l'écran `/comptabilite/paiements` : filtre texte (code/référence),
     * restreignable à une année scolaire (via le dossier lié) et à un caissier (ses propres
     * encaissements seulement — voir `PaiementController`).
     */
    @Query("""
        SELECT p FROM Paiement p
        WHERE (:filter IS NULL OR :filter = ''
               OR LOWER(p.code) LIKE LOWER(CONCAT('%', :filter, '%'))
               OR LOWER(p.reference) LIKE LOWER(CONCAT('%', :filter, '%')))
          AND (:anneeScolaireId IS NULL OR EXISTS (
                SELECT 1 FROM DossierEleve d
                WHERE d.id = p.dossierEleveId AND d.anneeScolaireId = :anneeScolaireId
              ))
          AND (:utilisateurId IS NULL OR p.utilisateurId = :utilisateurId)
        ORDER BY p.id DESC
        """)
    Page<Paiement> searchFiltered(
        @Param("filter") String filter,
        @Param("anneeScolaireId") Long anneeScolaireId,
        @Param("utilisateurId") Long utilisateurId,
        Pageable pageable
    );
}