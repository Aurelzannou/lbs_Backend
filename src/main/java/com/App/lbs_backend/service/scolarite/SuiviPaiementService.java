package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.response.SuiviFraisResponse;
import com.App.lbs_backend.dto.response.SuiviPaiementResponse;
import com.App.lbs_backend.dto.response.SuiviTrancheResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Echeancier;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.entity.TypeFrais;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.EcheancierRepository;
import com.App.lbs_backend.repository.FraisScolaireRepository;
import com.App.lbs_backend.repository.PaiementRepository;
import com.App.lbs_backend.repository.TypeFraisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Calcul pur du reste à payer d'un dossier d'inscription : pour chaque FraisScolaire de la
 * classe/année du dossier, compare le montant dû aux paiements SUCCES déjà enregistrés, puis
 * impute ce cumul en cascade (FIFO) sur les tranches d'Echeancier pour en déduire un statut.
 */
@Service
@RequiredArgsConstructor
public class SuiviPaiementService {

    private final DossierEleveRepository dossierEleveRepository;
    private final FraisScolaireRepository fraisScolaireRepository;
    private final EcheancierRepository echeancierRepository;
    private final PaiementRepository paiementRepository;
    private final TypeFraisRepository typeFraisRepository;

    /** Code du type « frais d'inscription » (payé pendant le parcours d'inscription, pas ici). */
    @org.springframework.beans.factory.annotation.Value("${fedapay.type-frais-inscription:INSCRIPTION}")
    private String typeFraisInscriptionCode;

    @Transactional(readOnly = true)
    public SuiviPaiementResponse getSuiviParDossier(Long dossierEleveId) {
        return getSuiviParDossier(dossierEleveId, false);
    }

    /**
     * @param exclureInscription true pour le portail parent : les frais d'inscription se règlent
     *                            pendant le parcours d'inscription, pas dans « Frais & paiements ».
     */
    @Transactional(readOnly = true)
    public SuiviPaiementResponse getSuiviParDossier(Long dossierEleveId, boolean exclureInscription) {
        DossierEleve dossier = dossierEleveRepository.findById(dossierEleveId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable"));

        Long typeInscriptionId = exclureInscription
                ? typeFraisRepository.findByCode(typeFraisInscriptionCode).map(t -> ((com.App.lbs_backend.entity.TypeFrais) t).getId()).orElse(null)
                : null;

        List<FraisScolaire> fraisList = fraisScolaireRepository
                .findByClasseIdAndAnneeScolaireId(dossier.getClasseId(), dossier.getAnneeScolaireId())
                .stream()
                // Un frais est actif sauf s'il est explicitement désactivé (actif == false).
                // Le formulaire n'ayant pas de case "actif", la plupart des frais ont actif == null.
                .filter(f -> !Boolean.FALSE.equals(f.getActif()))
                .filter(f -> typeInscriptionId == null || !typeInscriptionId.equals(f.getTypeFraisId()))
                .toList();

        double totalDu = 0;
        double totalPaye = 0;
        List<SuiviFraisResponse> fraisResponses = new java.util.ArrayList<>();

        for (FraisScolaire frais : fraisList) {
            double montantDu = frais.getMontant() != null ? frais.getMontant() : 0.0;
            double montantPaye = paiementRepository
                    .findByDossierEleveIdAndFraisScolaireIdAndStatutTransaction(dossierEleveId, frais.getId(), "SUCCES")
                    .stream().mapToDouble(Paiement::getMontant).sum();
            double reste = montantDu - montantPaye;

            String typeFraisLibelle = typeFraisRepository.findById(frais.getTypeFraisId())
                    .map(TypeFrais::getLibelle).orElse(null);

            List<Echeancier> echeanciers = echeancierRepository
                    .findByFraisScolaireIdOrderByNumeroAsc(frais.getId());

            List<SuiviTrancheResponse> tranches = new java.util.ArrayList<>();
            double restantAAllouer = montantPaye;
            LocalDate aujourdhui = LocalDate.now();
            for (Echeancier echeance : echeanciers) {
                double montantTranche = echeance.getMontant() != null ? echeance.getMontant() : 0.0;
                double alloue = Math.max(0, Math.min(restantAAllouer, montantTranche));
                restantAAllouer -= alloue;

                String statut;
                if (alloue >= montantTranche - 0.01) {
                    statut = "PAYEE";
                } else if (echeance.getDateEcheance() != null && echeance.getDateEcheance().isBefore(aujourdhui)) {
                    statut = "EN_RETARD";
                } else if (alloue > 0) {
                    statut = "PARTIELLE";
                } else {
                    statut = "EN_ATTENTE";
                }

                tranches.add(new SuiviTrancheResponse(
                        echeance.getId(), echeance.getNumero(), echeance.getLibelle(),
                        echeance.getDateEcheance(), montantTranche, alloue, statut));
            }

            fraisResponses.add(new SuiviFraisResponse(
                    frais.getId(), typeFraisLibelle, montantDu, montantPaye, reste, tranches));

            totalDu += montantDu;
            totalPaye += montantPaye;
        }

        return new SuiviPaiementResponse(dossierEleveId, totalDu, totalPaye, totalDu - totalPaye, fraisResponses);
    }
}
