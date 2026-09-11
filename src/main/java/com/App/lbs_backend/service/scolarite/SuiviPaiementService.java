package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.utils.ReportService;
import com.App.lbs_backend.dto.response.ImpayeLignePdf;
import com.App.lbs_backend.dto.response.SuiviFraisResponse;
import com.App.lbs_backend.dto.response.SuiviGlobalLigneResponse;
import com.App.lbs_backend.dto.response.SuiviPaiementResponse;
import com.App.lbs_backend.dto.response.SuiviTrancheResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Echeancier;
import com.App.lbs_backend.entity.FraisScolaire;
import com.App.lbs_backend.entity.Paiement;
import com.App.lbs_backend.entity.TypeFrais;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.EcheancierRepository;
import com.App.lbs_backend.repository.FraisScolaireRepository;
import com.App.lbs_backend.repository.PaiementRepository;
import com.App.lbs_backend.repository.TypeFraisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final ClasseRepository classeRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final ReportService reportService;

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
            // Le reste à payer ne descend jamais sous 0 (un éventuel trop-perçu se règle par
            // annulation d'un paiement, pas par un « reste » négatif).
            double reste = Math.max(0, montantDu - montantPaye);

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

        return new SuiviPaiementResponse(dossierEleveId, totalDu, totalPaye,
                Math.max(0, totalDu - totalPaye), fraisResponses);
    }

    /**
     * Vue globale « Tous les impayés » : tous les dossiers scolarisés (accepté/inscrit) dont le
     * reste à payer est strictement positif, triés par reste décroissant — pour repérer les plus
     * gros impayés en premier. `anneeScolaireId` null = toutes années confondues (c'est tout
     * l'intérêt de cette vue par rapport au suivi élève par élève, cantonné à l'année active).
     */
    @Transactional(readOnly = true)
    public List<SuiviGlobalLigneResponse> listerImpayes(Long anneeScolaireId, Long classeId, String statutFiltre) {
        List<DossierEleve> dossiers = dossierEleveRepository.findDossiersActifsPourSuivi(anneeScolaireId, classeId);
        List<SuiviGlobalLigneResponse> resultat = new ArrayList<>();

        for (DossierEleve dossier : dossiers) {
            SuiviPaiementResponse suivi = getSuiviParDossier(dossier.getId());
            if (suivi.totalReste() == null || suivi.totalReste() <= 0) continue;

            String statut = determinerStatutGlobal(suivi);
            if (statutFiltre != null && !statutFiltre.isBlank() && !statutFiltre.equalsIgnoreCase(statut)) continue;

            String nomComplet = ((dossier.getNom() != null ? dossier.getNom() : "") + " "
                    + (dossier.getPrenom() != null ? dossier.getPrenom() : "")).trim();
            String classeLibelle = dossier.getClasse() != null ? dossier.getClasse().getLibelle() : "—";
            String anneeLibelle = dossier.getAnneeScolaire() != null ? dossier.getAnneeScolaire().getLibelle() : "—";

            resultat.add(new SuiviGlobalLigneResponse(
                    dossier.getId(), nomComplet, classeLibelle, anneeLibelle,
                    suivi.totalDu(), suivi.totalPaye(), suivi.totalReste(), statut));
        }

        resultat.sort(Comparator.comparingDouble(SuiviGlobalLigneResponse::totalReste).reversed());
        return resultat;
    }

    /** EN_RETARD si au moins une échéance est dépassée sans être soldée, PARTIELLE si une partie
        a déjà été payée, EN_ATTENTE sinon (rien payé du tout, aucune échéance encore en retard). */
    private String determinerStatutGlobal(SuiviPaiementResponse suivi) {
        boolean enRetard = suivi.frais().stream()
                .flatMap(f -> f.tranches().stream())
                .anyMatch(t -> "EN_RETARD".equals(t.statut()));
        if (enRetard) return "EN_RETARD";
        return (suivi.totalPaye() != null && suivi.totalPaye() > 0) ? "PARTIELLE" : "EN_ATTENTE";
    }

    /** PDF de la liste des impayés (mêmes filtres que {@link #listerImpayes}). */
    public byte[] genererImpayesPdf(Long anneeScolaireId, Long classeId, String statutFiltre) {
        List<SuiviGlobalLigneResponse> lignes = listerImpayes(anneeScolaireId, classeId, statutFiltre);

        NumberFormat montantFormat = NumberFormat.getIntegerInstance(Locale.FRANCE);
        List<ImpayeLignePdf> lignesPdf = new ArrayList<>();
        double totalDu = 0, totalPaye = 0, totalReste = 0;
        for (SuiviGlobalLigneResponse l : lignes) {
            lignesPdf.add(new ImpayeLignePdf(
                    l.eleveNomComplet(), l.classeLibelle(), l.anneeScolaireLibelle(),
                    montantFormat.format(l.totalDu()), montantFormat.format(l.totalPaye()),
                    montantFormat.format(l.totalReste()), libelleStatut(l.statut())));
            totalDu += l.totalDu() != null ? l.totalDu() : 0;
            totalPaye += l.totalPaye() != null ? l.totalPaye() : 0;
            totalReste += l.totalReste() != null ? l.totalReste() : 0;
        }

        String classeLibelle = classeId != null
                ? classeRepository.findById(classeId).map(Classe::getLibelle).orElse("—")
                : "Toutes les classes";
        String anneeLibelle = anneeScolaireId != null
                ? anneeScolaireRepository.findById(anneeScolaireId).map(AnneeScolaire::getLibelle).orElse("—")
                : "Toutes les années";

        Map<String, Object> params = new HashMap<>();
        params.put("classe", classeLibelle);
        params.put("anneeScolaire", anneeLibelle);
        params.put("effectif", String.valueOf(lignesPdf.size()));
        params.put("totalDu", montantFormat.format(totalDu));
        params.put("totalPaye", montantFormat.format(totalPaye));
        params.put("totalReste", montantFormat.format(totalReste));
        params.put("dateGeneration", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        try {
            return reportService.generatePdfReport("liste-impayes", params, lignesPdf);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer la liste des impayés : " + e.getMessage(), e);
        }
    }

    private String libelleStatut(String code) {
        return switch (code) {
            case "EN_RETARD" -> "En retard";
            case "PARTIELLE" -> "Partiel";
            default -> "En attente";
        };
    }
}
