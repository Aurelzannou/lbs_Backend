package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.SoumettreInscriptionRequest;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
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

    /**
     * Soumet une inscription complète :
     * 1. Crée l'élève si nécessaire
     * 2. Crée le dossier avec statut DEPOSE
     */
    @Transactional
    public DossierEleveResponse soumettre(SoumettreInscriptionRequest request) {
        log.info("Soumission inscription — classe:{} année:{}", request.getClasseId(), request.getAnneeScolaireId());

        Long eleveId = resolveEleveId(request);

        DossierEleve dossier = new DossierEleve();
        dossier.setEleveId(eleveId);
        dossier.setClasseId(request.getClasseId());
        dossier.setAnneeScolaireId(request.getAnneeScolaireId());
        dossierEleveService.setStatutDepose(dossier);

        DossierEleve saved = dossierEleveService.create(dossier);
        log.info("Dossier créé — uuid:{}", saved.getUuid());
        return dossierEleveService.toResponse(saved.getId());
    }

    private Long resolveEleveId(SoumettreInscriptionRequest request) {
        if (request.getEleveId() != null) {
            return request.getEleveId();
        }
        Eleve eleve = new Eleve();
        eleve.setNom(request.getNom());
        eleve.setPrenom(request.getPrenom());
        eleve.setSexe(request.getSexe());
        eleve.setDateNaissance(request.getDateNaissance());
        eleve.setTuteurId(request.getTuteurId());
        return eleveService.create(eleve).getId();
    }
}
