package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.utils.ReportService;
import com.App.lbs_backend.dto.request.ReorganiserJourRequest;
import com.App.lbs_backend.dto.response.EmploiDuTempsLignePdf;
import com.App.lbs_backend.dto.response.EmploiDuTempsResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.entity.EmploiDuTemps;
import com.App.lbs_backend.mapper.EmploiDuTempsMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.EmploiDuTempsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmploiDuTempsService extends AbstractBaseService<EmploiDuTemps, EmploiDuTempsResponse> {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final EmploiDuTempsMapper emploiDuTempsMapper;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final ClasseRepository classeRepository;
    private final ReportService reportService;

    /** Ordre d'affichage des jours dans le brouillon (les valeurs stockées sont en majuscules). */
    private static final List<String> ORDRE_JOURS =
            List.of("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI", "DIMANCHE");

    public EmploiDuTempsService(EmploiDuTempsRepository emploiDuTempsRepository, EmploiDuTempsMapper emploiDuTempsMapper,
                                AnneeScolaireRepository anneeScolaireRepository, ClasseRepository classeRepository,
                                ReportService reportService) {
        super(EmploiDuTemps.class);
        this.emploiDuTempsRepository = emploiDuTempsRepository;
        this.emploiDuTempsMapper = emploiDuTempsMapper;
        this.anneeScolaireRepository = anneeScolaireRepository;
        this.classeRepository = classeRepository;
        this.reportService = reportService;
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

    /** Combine recherche + mapping dans UNE seule transaction : le mapper accède à des relations
        LAZY (classe, année scolaire, matière, professeur) qui nécessitent une session Hibernate
        ouverte — fetch puis mapping séparés (ex. directement dans un controller) échoue en prod
        (open-in-view=false) dès que ces relations sont renseignées. */
    @Transactional(readOnly = true)
    public List<EmploiDuTempsResponse> listerResponses(Long classeId, Long anneeScolaireId) {
        return findByClasseIdAndAnnee(classeId, anneeScolaireId).stream()
                .map(e -> mapper().toResponse(e))
                .toList();
    }

    /**
     * Brouillon PDF de l'emploi du temps d'une classe : les séances regroupées par jour (dans
     * l'ordre naturel de la semaine, pas l'ordre alphabétique), puis par heure. Sert de support
     * de travail imprimable — d'où la mention « BROUILLON » en en-tête.
     */
    @Transactional(readOnly = true)
    public byte[] genererBrouillonPdf(Long classeId, Long anneeScolaireId) {
        List<EmploiDuTemps> seances = new ArrayList<>(findByClasseIdAndAnnee(classeId, anneeScolaireId));
        seances.sort(Comparator
                .comparingInt((EmploiDuTemps s) -> {
                    int i = ORDRE_JOURS.indexOf(s.getJour() == null ? "" : s.getJour().toUpperCase());
                    return i < 0 ? Integer.MAX_VALUE : i;
                })
                .thenComparing(s -> s.getHeureDebut() == null ? LocalTime.MIN : s.getHeureDebut()));

        DateTimeFormatter hf = DateTimeFormatter.ofPattern("HH:mm");
        List<EmploiDuTempsLignePdf> lignes = seances.stream()
                .map(s -> new EmploiDuTempsLignePdf(
                        capitaliser(s.getJour()),
                        (s.getHeureDebut() != null ? hf.format(s.getHeureDebut()) : "?")
                                + " - " + (s.getHeureFin() != null ? hf.format(s.getHeureFin()) : "?"),
                        s.getMatiere() != null ? s.getMatiere().getLibelle() : "—",
                        s.getProfesseur() != null
                                ? s.getProfesseur().getNom() + " " + s.getProfesseur().getPrenom()
                                : "Non attribué"))
                .toList();

        String classeLibelle = classeRepository.findById(classeId)
                .map(c -> c.getLibelle() + (c.getCode() != null ? " (" + c.getCode() + ")" : ""))
                .orElse("Classe #" + classeId);
        String anneeLibelle = anneeScolaireRepository.findById(anneeScolaireId)
                .map(AnneeScolaire::getLibelle).orElse("");

        Map<String, Object> params = new HashMap<>();
        params.put("classe", classeLibelle);
        params.put("anneeScolaire", anneeLibelle);
        params.put("dateGeneration", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        params.put("nbSeances", String.valueOf(lignes.size()));

        try {
            return reportService.generatePdfReport("emploi-du-temps", params, lignes);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Impossible de générer le PDF de l'emploi du temps : " + e.getMessage(), e);
        }
    }

    private String capitaliser(String s) {
        if (s == null || s.isBlank()) return "";
        String low = s.toLowerCase();
        return Character.toUpperCase(low.charAt(0)) + low.substring(1);
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
