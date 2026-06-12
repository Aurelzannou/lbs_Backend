package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.StatutInscription;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrateur des opérations de validation des dossiers d'inscription.
 * Centralise : accepter, refuser, inscrire, consulter les dossiers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ValidationService {

    private final DossierEleveService         dossierEleveService;
    private final DossierEleveRepository      dossierEleveRepository;
    private final StatutInscriptionRepository statutRepository;
    private final EleveRepository             eleveRepository;
    private final TuteurRepository            tuteurRepository;
    private final EmailService                emailService;

    @Transactional
    public DossierEleveResponse accepter(String uuid) {
        log.info("Acceptation du dossier : {}", uuid);
        return changerStatutEtNotifier(uuid, "ACCEPTE", null);
    }

    @Transactional
    public DossierEleveResponse refuser(String uuid, String motif) {
        log.info("Refus du dossier : {}", uuid);
        return changerStatutEtNotifier(uuid, "REFUSE", motif);
    }

    @Transactional
    public DossierEleveResponse inscrire(String uuid) {
        log.info("Inscription confirmée pour le dossier : {}", uuid);
        return changerStatut(uuid, "INSCRIT");
    }

    /**
     * Retourne les dossiers du tuteur identifié par son email (depuis le JWT).
     */
    public List<DossierEleveResponse> getMesDossiers(String email) {
        return tuteurRepository.findByEmail(email)
                .map(tuteur -> dossierEleveRepository
                        .findByTuteurId(tuteur.getId())
                        .stream()
                        .map(d -> dossierEleveService.mapper().toResponse(d))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    // ─────────────────────────────────────────────────────────────────────────

    private DossierEleveResponse changerStatutEtNotifier(String uuid, String statutCode, String motif) {
        DossierEleve dossier = dossierEleveService.findByUuid(uuid);
        StatutInscription statut = statutRepository.findByCode(statutCode)
                .orElseThrow(() -> new RuntimeException("Statut non trouvé : " + statutCode));
        dossier.setStatutId(statut.getId());
        dossierEleveService.update(dossier);

        envoyerNotification(dossier, statutCode, motif);
        return dossierEleveService.toResponse(dossier.getId());
    }

    private DossierEleveResponse changerStatut(String uuid, String statutCode) {
        return changerStatutEtNotifier(uuid, statutCode, null);
    }

    private void envoyerNotification(DossierEleve dossier, String statutCode, String motif) {
        if (!List.of("ACCEPTE", "REFUSE").contains(statutCode)) return;
        try {
            Eleve eleve = eleveRepository.findById(dossier.getEleveId()).orElse(null);
            if (eleve == null || eleve.getTuteurId() == null) return;

            Tuteur tuteur = tuteurRepository.findById(eleve.getTuteurId()).orElse(null);
            if (tuteur == null || tuteur.getEmail() == null) return;

            String classe = dossier.getClasse() != null ? dossier.getClasse().getLibelle() : "—";
            String annee  = dossier.getAnneeScolaire() != null ? dossier.getAnneeScolaire().getLibelle() : "—";
            String numero = dossier.getNumero() != null ? dossier.getNumero() : "—";

            if ("ACCEPTE".equals(statutCode)) {
                emailService.sendDossierAccepte(tuteur.getEmail(),
                        tuteur.getNom(), tuteur.getPrenom(),
                        eleve.getNom(), eleve.getPrenom(), classe, annee, numero);
            } else {
                emailService.sendDossierRefuse(tuteur.getEmail(),
                        tuteur.getNom(), tuteur.getPrenom(),
                        eleve.getNom(), eleve.getPrenom(), classe, annee, numero, motif);
            }
        } catch (Exception e) {
            log.error("Erreur notification email dossier {} : {}", dossier.getUuid(), e.getMessage());
        }
    }
}
