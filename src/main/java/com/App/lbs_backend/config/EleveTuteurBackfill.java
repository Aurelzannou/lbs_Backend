package com.App.lbs_backend.config;

import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.EleveTuteur;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.EleveTuteurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Amorce la table de liaison {@code lbs_eleve_tuteur} à partir des liens directs historiques
 * {@code Eleve.tuteurId} (posés quand un parent dépose lui-même le dossier). Idempotent :
 * ne crée que les lignes manquantes, s'exécute à chaque démarrage sans effet de bord.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EleveTuteurBackfill {

    private final EleveRepository eleveRepository;
    private final EleveTuteurRepository eleveTuteurRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        int crees = 0;
        for (Eleve eleve : eleveRepository.findAll()) {
            if (eleve.getTuteurId() == null) continue;
            if (eleveTuteurRepository.findByEleveIdAndTuteurId(eleve.getId(), eleve.getTuteurId()).isPresent()) {
                continue;
            }
            EleveTuteur lien = new EleveTuteur();
            lien.setEleveId(eleve.getId());
            lien.setTuteurId(eleve.getTuteurId());
            lien.setContactUrgence(true);
            lien.setCode("ELT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            eleveTuteurRepository.save(lien);
            crees++;
        }
        if (crees > 0) {
            log.info("[backfill] {} lien(s) EleveTuteur créé(s) depuis Eleve.tuteurId", crees);
        }
    }
}
