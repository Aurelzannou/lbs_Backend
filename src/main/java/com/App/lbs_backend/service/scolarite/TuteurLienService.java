package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.entity.EleveTuteur;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.EleveTuteurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Résout l'ensemble des élèves visibles par un tuteur au portail parent.
 *
 * Deux sources cumulées :
 *  - la table de liaison {@link EleveTuteur} (association posée par l'administration) ;
 *  - le lien direct historique {@code Eleve.tuteurId} (posé quand le parent dépose lui-même
 *    le dossier d'inscription) — conservé en secours, aucune migration obligatoire.
 */
@Service
@RequiredArgsConstructor
public class TuteurLienService {

    private final EleveTuteurRepository eleveTuteurRepository;
    private final EleveRepository eleveRepository;

    /** Ids des élèves rattachés au tuteur (union des deux liens). Jamais {@code null}. */
    public Set<Long> eleveIdsDuTuteur(Long tuteurId) {
        Set<Long> ids = new HashSet<>();
        eleveTuteurRepository.findByTuteurId(tuteurId).stream()
                .map(EleveTuteur::getEleveId)
                .forEach(ids::add);
        eleveRepository.findByTuteurId(tuteurId).stream()
                .map(Eleve::getId)
                .forEach(ids::add);
        return ids;
    }
}
