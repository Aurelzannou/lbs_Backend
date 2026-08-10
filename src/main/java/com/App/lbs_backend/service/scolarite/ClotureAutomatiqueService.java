package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.PeriodeAcademiqueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Filet de sécurité : si un professeur oublie de cliquer "Soumettre" avant la fin d'une période,
    l'admin resterait bloqué indéfiniment sur l'écran de validation des bulletins (une matière en
    BROUILLON n'y est consultable qu'en lecture seule, jamais validable). Une fois la période
    terminée, ce job soumet automatiquement à sa place toute matière encore en BROUILLON — l'admin
    garde la main pour vérifier et valider lui-même, l'auto-soumission ne fait que débloquer
    l'étape suivante du workflow. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClotureAutomatiqueService {

    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final ClasseRepository classeRepository;
    private final ProgressionSaisieNoteService progressionSaisieNoteService;

    @Scheduled(cron = "0 0 2 * * *")
    public void autoSoumettrePeriodesTerminees() {
        List<PeriodeAcademique> periodesTerminees = periodeAcademiqueRepository.findByDateFinBefore(LocalDate.now());
        if (periodesTerminees.isEmpty()) return;

        List<Classe> classes = classeRepository.findAll();
        int compteur = 0;
        for (PeriodeAcademique periode : periodesTerminees) {
            for (Classe classe : classes) {
                List<Long> matiereIds = classe.getMatiereIds();
                if (matiereIds == null) continue;
                for (Long matiereId : matiereIds) {
                    try {
                        boolean aTransitionne = progressionSaisieNoteService
                                .soumettreAutomatiquementSiBrouillon(classe.getId(), matiereId, periode.getId());
                        if (aTransitionne) compteur++;
                    } catch (Exception e) {
                        log.error("Erreur auto-soumission classe {} matière {} période {} : {}",
                                classe.getId(), matiereId, periode.getId(), e.getMessage());
                    }
                }
            }
        }
        if (compteur > 0) {
            log.info("{} matière(s) auto-soumise(s) automatiquement pour cause de fin de période.", compteur);
        }
    }
}
