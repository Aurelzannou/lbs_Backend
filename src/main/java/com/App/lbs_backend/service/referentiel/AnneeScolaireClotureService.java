package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Maintient automatiquement le statut « Actif / Inactif » des années scolaires :
 *  - une année dont la date de fin est dépassée passe Inactive ;
 *  - si plus aucune année n'est active, on active celle dont la période contient la date du jour
 *    (uniquement s'il y en a exactement une — sinon on laisse l'admin décider).
 *
 * Exécuté au démarrage puis chaque nuit à 02h05.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnneeScolaireClotureService {

    private final AnneeScolaireRepository anneeScolaireRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 2 * * *")
    @Transactional
    public void synchroniserStatutAnnees() {
        LocalDate today = LocalDate.now();
        List<AnneeScolaire> toutes = anneeScolaireRepository.findAll();

        int desactivees = 0;
        for (AnneeScolaire a : toutes) {
            if (Boolean.TRUE.equals(a.getActif())
                    && a.getDateFin() != null && a.getDateFin().isBefore(today)) {
                a.setActif(false);
                anneeScolaireRepository.save(a);
                desactivees++;
                log.info("Année scolaire « {} » désactivée automatiquement (terminée le {}).",
                        a.getLibelle(), a.getDateFin());
            }
        }

        boolean uneActive = toutes.stream().anyMatch(a -> Boolean.TRUE.equals(a.getActif()));
        if (!uneActive) {
            List<AnneeScolaire> courantes = toutes.stream()
                    .filter(a -> a.getDateDebut() != null && a.getDateFin() != null
                            && !today.isBefore(a.getDateDebut()) && !today.isAfter(a.getDateFin()))
                    .toList();
            if (courantes.size() == 1) {
                AnneeScolaire a = courantes.get(0);
                a.setActif(true);
                anneeScolaireRepository.save(a);
                log.info("Année scolaire « {} » activée automatiquement (période en cours).", a.getLibelle());
            }
        }

        if (desactivees > 0) {
            log.info("{} année(s) scolaire(s) désactivée(s) automatiquement.", desactivees);
        }
    }
}
