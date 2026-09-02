package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.SoumettreInscriptionRequest;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import com.App.lbs_backend.service.referentiel.PeriodeInscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrateur du workflow d'inscription.
 * Point d'entrée unique pour la soumission d'un dossier depuis le portail parent.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InscriptionService {

    private final EleveService                eleveService;
    private final DossierEleveService         dossierEleveService;
    private final StatutInscriptionRepository statutRepository;
    private final PeriodeInscriptionService   periodeInscriptionService;

    /**
     * Soumet une inscription complète :
     * 1. Crée le dossier avec statut DEPOSE (l'Eleve n'est créé qu'à l'acceptation, voir
     *    ValidationService.accepter() — le dossier porte lui-même l'identité du candidat
     *    en attendant).
     */
    @Transactional
    public DossierEleveResponse soumettre(SoumettreInscriptionRequest request) {
        log.info("Soumission inscription — classe:{} année:{}", request.getClasseId(), request.getAnneeScolaireId());
        if (request.getAnneeScolaireId() != null) {
            periodeInscriptionService.validerPeriode(request.getAnneeScolaireId());
        }

        DossierEleve dossier = new DossierEleve();
        dossier.setClasseId(request.getClasseId());
        dossier.setAnneeScolaireId(request.getAnneeScolaireId());
        dossier.setTuteurId(request.getTuteurId());

        // Un enfant ne peut avoir qu'un seul dossier vivant par année scolaire.
        String nomRef = request.getNom();
        String prenomRef = request.getPrenom();
        if (request.getEleveId() != null) {
            Eleve ref = eleveService.findById(request.getEleveId());
            nomRef = ref.getNom();
            prenomRef = ref.getPrenom();
        }
        dossierEleveService.verifierUnSeulDossierParAnnee(
                request.getEleveId(), nomRef, prenomRef, request.getAnneeScolaireId(), null);

        if (request.getEleveId() != null) {
            // Enfant déjà inscrit par le passé : on garde le lien direct et on recopie
            // son identité pour un affichage cohérent dès le dépôt.
            Eleve existant = eleveService.findById(request.getEleveId());
            dossier.setEleveId(existant.getId());
            dossier.setNom(existant.getNom());
            dossier.setPrenom(existant.getPrenom());
            dossier.setSexe(existant.getSexe());
            dossier.setDateNaissance(existant.getDateNaissance());
            dossier.setSouffrant(existant.getSouffrant());
            dossier.setProvenance(existant.getProvenance());
        } else {
            dossier.setNom(request.getNom());
            dossier.setPrenom(request.getPrenom());
            dossier.setSexe(request.getSexe());
            dossier.setDateNaissance(request.getDateNaissance());
            dossier.setSouffrant(request.getSouffrant());
            dossier.setProvenance(request.getProvenance());
        }

        dossierEleveService.setStatutDepose(dossier);
        // Sur une réinscription, request.nom/prenom sont nuls — on prend l'identité du dossier.
        dossier.setNumero(dossierEleveService.genererNumero(dossier.getNom(), dossier.getPrenom()));

        DossierEleve saved = dossierEleveService.create(dossier);
        log.info("Dossier créé — uuid:{}", saved.getUuid());
        return dossierEleveService.toResponse(saved.getId());
    }
}
