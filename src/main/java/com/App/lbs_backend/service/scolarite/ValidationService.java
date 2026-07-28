package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.config.RabbitMQConfig;
import com.App.lbs_backend.dto.message.DossierNotificationMessage;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.StatutInscription;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Orchestrateur des opérations de validation des dossiers d'inscription.
 * Centralise : accepter, refuser, consulter les dossiers.
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
    private final RabbitTemplate              rabbitTemplate;
    private final HistoriqueService           historiqueService;

    @Transactional
    public DossierEleveResponse accepter(String uuid) {
        log.info("Acceptation du dossier : {}", uuid);
        DossierEleve dossier = dossierEleveService.findByUuid(uuid);
        creerEleveSiAbsent(dossier);
        return changerStatutEtNotifier(dossier, "ACCEPTE", null);
    }

    @Transactional
    public DossierEleveResponse refuser(String uuid, String motif) {
        log.info("Refus du dossier : {}", uuid);
        DossierEleve dossier = dossierEleveService.findByUuid(uuid);
        return changerStatutEtNotifier(dossier, "REFUSE", motif);
    }

    /**
     * Liste les dossiers filtrés par statut, année scolaire et recherche texte.
     */
    public List<DossierEleveResponse> listerDossiers(Optional<String> statutCode, Long anneeId, Long classeId, String filter) {
        List<DossierEleve> dossiers = statutCode
                .filter(s -> !s.isBlank())
                .flatMap(s -> statutRepository.findByCode(s))
                .map(st -> dossierEleveRepository.findByStatutIdFiltered(
                        st.getId(),
                        anneeId,
                        classeId,
                        (filter != null && !filter.isBlank()) ? filter : null))
                .orElse(Collections.emptyList());

        return dossiers.stream()
                .map(d -> dossierEleveService.mapper().toResponse(d))
                .collect(Collectors.toList());
    }

    /**
     * Retourne les dossiers du tuteur identifié par son email (depuis le JWT).
     */
    public List<DossierEleveResponse> getMesDossiers(String email) {
        log.info("[mes-dossiers] email extrait du JWT : '{}'", email);
        return tuteurRepository.findByEmail(email)
                .map(tuteur -> {
                    log.info("[mes-dossiers] tuteur trouvé : id={} email={}", tuteur.getId(), tuteur.getEmail());
                    List<DossierEleve> dossiers = dossierEleveRepository.findByTuteurId(tuteur.getId());
                    log.info("[mes-dossiers] {} dossier(s) trouvé(s) pour tuteurId={}", dossiers.size(), tuteur.getId());
                    return dossiers.stream()
                            .map(d -> dossierEleveService.mapper().toResponse(d))
                            .collect(Collectors.toList());
                })
                .orElseGet(() -> {
                    log.warn("[mes-dossiers] aucun tuteur trouvé pour email='{}'", email);
                    return Collections.emptyList();
                });
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Crée réellement la ligne Eleve au moment de l'acceptation, si le dossier n'en a pas
     * déjà un (cas d'un nouveau candidat — un enfant déjà inscrit par le passé a déjà son
     * eleveId dès le dépôt). En profite pour renseigner la classe sur l'Eleve.
     */
    private void creerEleveSiAbsent(DossierEleve dossier) {
        if (dossier.getEleveId() != null) return;

        Eleve eleve = new Eleve();
        eleve.setNom(dossier.getNom());
        eleve.setPrenom(dossier.getPrenom());
        eleve.setSexe(dossier.getSexe());
        eleve.setDateNaissance(dossier.getDateNaissance());
        eleve.setSouffrant(dossier.getSouffrant());
        eleve.setProvenance(dossier.getProvenance());
        eleve.setTuteurId(dossier.getTuteurId());
        eleve.setClasseId(dossier.getClasseId());
        Eleve saved = eleveRepository.save(eleve);

        dossier.setEleveId(saved.getId());
        log.info("Eleve créé à l'acceptation du dossier {} : eleveId={}", dossier.getUuid(), saved.getId());
    }

    private DossierEleveResponse changerStatutEtNotifier(DossierEleve dossier, String statutCode, String motif) {
        StatutInscription statut = statutRepository.findByCode(statutCode)
                .orElseThrow(() -> new RuntimeException("Statut non trouvé : " + statutCode));
        dossier.setStatutId(statut.getId());
        dossierEleveService.update(dossier);

        historiqueService.enregistrer(dossier.getId(), statutCode, "admin", motif);
        envoyerNotification(dossier, statutCode, motif);
        return dossierEleveService.toResponse(dossier.getId());
    }

    private void envoyerNotification(DossierEleve dossier, String statutCode, String motif) {
        if (!List.of("ACCEPTE", "REFUSE").contains(statutCode)) return;
        try {
            if (dossier.getTuteurId() == null) return;

            Tuteur tuteur = tuteurRepository.findById(dossier.getTuteurId()).orElse(null);
            if (tuteur == null || tuteur.getEmail() == null) return;

            String classe = dossier.getClasse() != null ? dossier.getClasse().getLibelle() : "—";
            String annee  = dossier.getAnneeScolaire() != null ? dossier.getAnneeScolaire().getLibelle() : "—";
            String numero = dossier.getNumero() != null ? dossier.getNumero() : "—";

            DossierNotificationMessage message = new DossierNotificationMessage(
                    statutCode, tuteur.getEmail(), tuteur.getNom(), tuteur.getPrenom(),
                    dossier.getNom(), dossier.getPrenom(), classe, annee, numero, motif);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("Erreur notification email dossier {} : {}", dossier.getUuid(), e.getMessage());
        }
    }
}
