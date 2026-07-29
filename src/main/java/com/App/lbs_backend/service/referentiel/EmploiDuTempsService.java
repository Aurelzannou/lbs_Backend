package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.request.ReorganiserJourRequest;
import com.App.lbs_backend.dto.response.EmploiDuTempsResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.entity.EmploiDuTemps;
import com.App.lbs_backend.mapper.EmploiDuTempsMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.EmploiDuTempsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmploiDuTempsService extends AbstractBaseService<EmploiDuTemps, EmploiDuTempsResponse> {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final EmploiDuTempsMapper emploiDuTempsMapper;
    private final AnneeScolaireRepository anneeScolaireRepository;

    public EmploiDuTempsService(EmploiDuTempsRepository emploiDuTempsRepository, EmploiDuTempsMapper emploiDuTempsMapper, AnneeScolaireRepository anneeScolaireRepository) {
        super(EmploiDuTemps.class);
        this.emploiDuTempsRepository = emploiDuTempsRepository;
        this.emploiDuTempsMapper = emploiDuTempsMapper;
        this.anneeScolaireRepository = anneeScolaireRepository;
    }

    @Override
    public BaseRepository<EmploiDuTemps> repository() {
        return emploiDuTempsRepository;
    }

    @Override
    public Mapper<EmploiDuTemps, EmploiDuTempsResponse> mapper() {
        return emploiDuTempsMapper;
    }

    public List<EmploiDuTemps> findByClasseIdAndAnnee(Long classeId, Long anneeScolaireId) {
        return emploiDuTempsRepository.findByClasseIdAndAnneeScolaireIdOrderByJourAscHeureDebutAsc(classeId, anneeScolaireId);
    }

    /** Nombre de cours (toutes classes/années confondues) actuellement attribués à ce professeur. */
    public long countCoursParProf(Long profId) {
        return emploiDuTempsRepository.countByProfId(profId);
    }

    /**
     * Vérifie qu'aucun cours existant (pour ce professeur ou cette classe, sur cette même année
     * scolaire) ne chevauche le créneau demandé. `excludeId` permet d'exclure la séance elle-même
     * lors d'une modification.
     */
    public void verifierConflits(Long classeId, Long anneeScolaireId, Long profId, String jour, LocalTime heureDebut, LocalTime heureFin, Long excludeId) {
        if (emploiDuTempsRepository.existsConflitProfesseur(profId, anneeScolaireId, jour, heureDebut, heureFin, excludeId)) {
            throw new IllegalArgumentException("Ce professeur a déjà un cours sur ce créneau.");
        }
        if (emploiDuTempsRepository.existsConflitClasse(classeId, anneeScolaireId, jour, heureDebut, heureFin, excludeId)) {
            throw new IllegalArgumentException("Cette classe a déjà un cours sur ce créneau.");
        }
    }

    /**
     * Une fois qu'une année scolaire n'est plus l'année active (elle est terminée / remplacée
     * par une nouvelle année), son emploi du temps devient figé : plus d'ajout, modification ou
     * suppression possible.
     */
    public void verifierAnneeModifiable(Long anneeScolaireId) {
        AnneeScolaire annee = anneeScolaireRepository.findById(anneeScolaireId).orElse(null);
        if (annee == null || !Boolean.TRUE.equals(annee.getActif())) {
            throw new IllegalArgumentException(
                    "Cette année scolaire n'est plus active : son emploi du temps ne peut plus être modifié.");
        }
    }

    /**
     * Réorganise en une seule transaction les cours d'un jour (glisser-déposer) : chaque cours
     * du lot reçoit son nouvel horaire, et le lot entier n'est validé que si aucun cours ne
     * chevauche un cours situé EN DEHORS du lot (le lot lui-même, par construction, est déjà
     * sans chevauchement puisque recalculé côté client). Ça évite les faux conflits transitoires
     * qu'on aurait avec des appels de mise à jour indépendants (un cours qui prend temporairement
     * la place d'un autre pas encore déplacé).
     */
    @Transactional
    public List<EmploiDuTemps> reorganiserJour(ReorganiserJourRequest form) {
        List<EmploiDuTemps> entites = new ArrayList<>();
        for (ReorganiserJourRequest.Creneau c : form.getSeances()) {
            entites.add(findByUuid(c.getUuid()));
        }

        if (!entites.isEmpty()) {
            verifierAnneeModifiable(entites.get(0).getAnneeScolaireId());
        }

        List<Long> idsDuLot = entites.stream().map(EmploiDuTemps::getId).toList();

        for (int i = 0; i < entites.size(); i++) {
            EmploiDuTemps e = entites.get(i);
            ReorganiserJourRequest.Creneau c = form.getSeances().get(i);

            if (emploiDuTempsRepository.existsConflitProfesseurHorsLot(
                    e.getProfId(), e.getAnneeScolaireId(), form.getJour(), c.getHeureDebut(), c.getHeureFin(), idsDuLot)) {
                throw new IllegalArgumentException("Ce professeur a déjà un cours sur ce créneau.");
            }
            if (emploiDuTempsRepository.existsConflitClasseHorsLot(
                    e.getClasseId(), e.getAnneeScolaireId(), form.getJour(), c.getHeureDebut(), c.getHeureFin(), idsDuLot)) {
                throw new IllegalArgumentException("Cette classe a déjà un cours sur ce créneau.");
            }
        }

        for (int i = 0; i < entites.size(); i++) {
            EmploiDuTemps e = entites.get(i);
            ReorganiserJourRequest.Creneau c = form.getSeances().get(i);
            e.setJour(form.getJour());
            e.setHeureDebut(c.getHeureDebut());
            e.setHeureFin(c.getHeureFin());
            update(e);
        }

        return entites;
    }
}
