package com.App.lbs_backend.service;

import com.App.lbs_backend.dto.response.BulletinResponse;
import com.App.lbs_backend.dto.response.DashboardStatsResponse;
import com.App.lbs_backend.dto.response.DashboardStatsResponse.AvancementClasse;
import com.App.lbs_backend.dto.response.DashboardStatsResponse.MatiereATraiter;
import com.App.lbs_backend.dto.response.DashboardStatsResponse.NotesBulletins;
import com.App.lbs_backend.dto.response.DashboardStatsResponse.PointMensuel;
import com.App.lbs_backend.dto.response.DashboardStatsResponse.Repartition;
import com.App.lbs_backend.entity.*;
import com.App.lbs_backend.repository.*;
import com.App.lbs_backend.service.scolarite.BulletinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/** Calcule les agrégats du tableau de bord administrateur (comptages + séries mensuelles). */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private static final int NB_MOIS = 6;

    private final EleveRepository eleveRepository;
    private final ProfesseurRepository professeurRepository;
    private final ClasseRepository classeRepository;
    private final DossierEleveRepository dossierEleveRepository;
    private final PaiementRepository paiementRepository;
    private final DepenseScolaireRepository depenseScolaireRepository;
    private final CaisseRepository caisseRepository;
    private final FraisScolaireRepository fraisScolaireRepository;
    private final StatutInscriptionRepository statutInscriptionRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final ProgressionSaisieNoteRepository progressionSaisieNoteRepository;
    private final ValidationBulletinRepository validationBulletinRepository;
    private final EtapeRepository etapeRepository;
    private final NoteRepository noteRepository;
    private final MatiereRepository matiereRepository;
    private final BulletinService bulletinService;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        AnneeScolaire anneeActive = anneeScolaireRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getActif()))
                .findFirst().orElse(null);
        Long anneeId = anneeActive != null ? anneeActive.getId() : null;
        LocalDate debutAnnee = anneeActive != null ? anneeActive.getDateDebut() : null;
        LocalDate finAnnee = anneeActive != null ? anneeActive.getDateFin() : null;

        long nbProfesseurs = professeurRepository.findAll().stream()
                .filter(p -> !Boolean.FALSE.equals(p.getActif()))
                .count();
        long nbClasses = classeRepository.count();

        Map<Long, String> statutCode = statutInscriptionRepository.findAll().stream()
                .collect(Collectors.toMap(StatutInscription::getId,
                        s -> s.getCode() != null ? s.getCode().toUpperCase() : "AUTRE"));

        // Toutes les statistiques ci-dessous sont limitées à l'année scolaire en cours.
        List<DossierEleve> dossiers = dossierEleveRepository.findAll().stream()
                .filter(d -> anneeId == null || anneeId.equals(d.getAnneeScolaireId()))
                .toList();
        Set<Long> dossierIdsAnnee = dossiers.stream()
                .map(DossierEleve::getId).collect(Collectors.toSet());

        // Élève « de l'année » = a un dossier vivant (ni refusé, ni annulé) sur l'année — même
        // règle que la liste /scolarite/eleves. Se limiter au statut INSCRIT sous-comptait les
        // élèves acceptés mais pas encore formellement "inscrits" (cas le plus courant en début
        // d'année, avant la validation finale des dossiers).
        long nbEleves = anneeId == null ? eleveRepository.count()
                : dossiers.stream()
                        .filter(d -> !List.of("REFUSE", "ANNULE").contains(statutCode.getOrDefault(d.getStatutId(), "")))
                        .map(DossierEleve::getEleveId).filter(Objects::nonNull).distinct().count();

        Map<String, Long> parStatut = dossiers.stream()
                .collect(Collectors.groupingBy(
                        d -> statutCode.getOrDefault(d.getStatutId(), "AUTRE"), Collectors.counting()));

        List<Paiement> paiements = paiementRepository.findAll().stream()
                .filter(p -> anneeId == null
                        || (p.getDossierEleveId() != null && dossierIdsAnnee.contains(p.getDossierEleveId())))
                .toList();
        List<Paiement> paiementsReussis = paiements.stream()
                .filter(p -> "SUCCES".equals(p.getStatutTransaction()))
                .toList();
        double totalEncaisse = paiementsReussis.stream()
                .mapToDouble(p -> p.getMontant() != null ? p.getMontant() : 0.0).sum();

        double totalDepenses = depenseScolaireRepository.findAll().stream()
                .filter(d -> !Boolean.TRUE.equals(d.getAnnule()))
                .filter(d -> dansPeriode(d.getDateDepense(), debutAnnee, finAnnee))
                .mapToDouble(d -> d.getMontant() != null ? d.getMontant() : 0.0).sum();

        double soldeCaisses = caisseRepository.findAll().stream()
                .mapToDouble(c -> c.getSolde() != null ? c.getSolde() : 0.0).sum();

        // Reste à payer global : montant des frais attendus des dossiers acceptés/inscrits − encaissé.
        Map<String, Double> fraisParClasseAnnee = fraisScolaireRepository.findAll().stream()
                .filter(f -> f.getMontant() != null && !Boolean.FALSE.equals(f.getActif()))
                .collect(Collectors.groupingBy(
                        f -> f.getClasseId() + "_" + f.getAnneeScolaireId(),
                        Collectors.summingDouble(FraisScolaire::getMontant)));
        double montantAttendu = dossiers.stream()
                .filter(d -> Set.of("ACCEPTE", "INSCRIT").contains(statutCode.getOrDefault(d.getStatutId(), "")))
                .mapToDouble(d -> fraisParClasseAnnee.getOrDefault(
                        d.getClasseId() + "_" + d.getAnneeScolaireId(), 0.0))
                .sum();
        double resteAPayerTotal = Math.max(0, montantAttendu - totalEncaisse);

        List<PointMensuel> inscriptionsParMois = serieMensuelle(
                dossiers, DossierEleve::getDateDebut, d -> 1.0);
        List<PointMensuel> encaissementsParMois = serieMensuelle(
                paiementsReussis, Paiement::getDatePaiement,
                p -> p.getMontant() != null ? p.getMontant() : 0.0);

        List<Repartition> dossiersParStatut = List.of(
                new Repartition("Déposé", parStatut.getOrDefault("DEPOSE", 0L)),
                new Repartition("Accepté", parStatut.getOrDefault("ACCEPTE", 0L)),
                new Repartition("Inscrit", parStatut.getOrDefault("INSCRIT", 0L)),
                new Repartition("Refusé", parStatut.getOrDefault("REFUSE", 0L))
        );

        Map<Long, String> classeLibelle = classeRepository.findAll().stream()
                .collect(Collectors.toMap(Classe::getId,
                        c -> c.getLibelle() != null ? c.getLibelle() : c.getCode()));
        // Effectif = dossiers acceptés/inscrits de l'année en cours, ventilés par classe.
        List<Repartition> effectifParClasse = (anneeId == null
                ? eleveRepository.findAll().stream()
                        .filter(e -> e.getClasseId() != null)
                        .collect(Collectors.groupingBy(Eleve::getClasseId, Collectors.counting()))
                : dossiers.stream()
                        .filter(d -> Set.of("ACCEPTE", "INSCRIT")
                                .contains(statutCode.getOrDefault(d.getStatutId(), "")))
                        .filter(d -> d.getClasseId() != null)
                        .collect(Collectors.groupingBy(DossierEleve::getClasseId, Collectors.counting())))
                .entrySet().stream()
                .map(en -> new Repartition(classeLibelle.getOrDefault(en.getKey(), "—"), en.getValue()))
                .sorted(Comparator.comparingLong(Repartition::valeur).reversed())
                .toList();

        NotesBulletins notesBulletins = calculerNotesBulletins(anneeActive);

        return new DashboardStatsResponse(
                anneeActive != null ? anneeActive.getLibelle() : "—",
                nbEleves, nbProfesseurs, nbClasses,
                parStatut.getOrDefault("DEPOSE", 0L),
                parStatut.getOrDefault("ACCEPTE", 0L),
                parStatut.getOrDefault("INSCRIT", 0L),
                parStatut.getOrDefault("REFUSE", 0L),
                dossiers.size(),
                totalEncaisse, totalDepenses, soldeCaisses, resteAPayerTotal,
                inscriptionsParMois, encaissementsParMois, dossiersParStatut, effectifParClasse,
                notesBulletins);
    }

    /** Avancement notes/bulletins de la période académique en cours + moyennes des classes validées. */
    private NotesBulletins calculerNotesBulletins(AnneeScolaire anneeActive) {
        if (anneeActive == null) return null;

        List<PeriodeAcademique> periodes = periodeAcademiqueRepository
                .findByAnneeScolaireIdOrderByDateDebutAsc(anneeActive.getId());
        if (periodes.isEmpty()) return null;

        LocalDate today = LocalDate.now();
        PeriodeAcademique periode = periodes.stream()
                .filter(p -> p.getDateDebut() != null && p.getDateFin() != null
                        && !today.isBefore(p.getDateDebut()) && !today.isAfter(p.getDateFin()))
                .findFirst()
                // sinon : la dernière période déjà commencée, sinon la première
                .orElseGet(() -> periodes.stream()
                        .filter(p -> p.getDateDebut() != null && !today.isBefore(p.getDateDebut()))
                        .reduce((a, b) -> b)
                        .orElse(periodes.get(0)));

        List<Classe> classes = classeRepository.findAll();
        long feuillesAttendues = classes.stream()
                .filter(c -> c.getMatiereIds() != null)
                .mapToLong(c -> c.getMatiereIds().size())
                .sum();

        Long etapeValideeId = etapeRepository.findByCode("VALIDEE").map(Etape::getId).orElse(null);
        Set<Long> etapesSoumises = etapeRepository.findByCode("SOUMISE").map(Etape::getId).stream()
                .collect(Collectors.toSet());
        if (etapeValideeId != null) etapesSoumises.add(etapeValideeId);

        List<ProgressionSaisieNote> progressions = progressionSaisieNoteRepository.findAll();

        long feuillesSoumises = progressions.stream()
                .filter(p -> periode.getId().equals(p.getPeriodeId()))
                .filter(p -> p.getEtapeId() != null && etapesSoumises.contains(p.getEtapeId()))
                .count();

        // Avancement de la validation des matières sur TOUTE l'année (matières × périodes).
        Set<Long> periodeIdsAnnee = periodes.stream().map(PeriodeAcademique::getId).collect(Collectors.toSet());
        Map<Long, Long> valideesParClasse = progressions.stream()
                .filter(p -> periodeIdsAnnee.contains(p.getPeriodeId()))
                .filter(p -> etapeValideeId != null && etapeValideeId.equals(p.getEtapeId()))
                .collect(Collectors.groupingBy(ProgressionSaisieNote::getClasseId, Collectors.counting()));

        List<AvancementClasse> validationParClasse = classes.stream()
                .filter(c -> c.getMatiereIds() != null && !c.getMatiereIds().isEmpty())
                .map(c -> new AvancementClasse(
                        c.getLibelle() != null ? c.getLibelle() : c.getCode(),
                        valideesParClasse.getOrDefault(c.getId(), 0L),
                        (long) c.getMatiereIds().size() * periodes.size()))
                .sorted(Comparator.comparing(AvancementClasse::classeLibelle,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        long matieresValideesAnnee = validationParClasse.stream()
                .mapToLong(AvancementClasse::matieresValidees).sum();
        long matieresAttenduesAnnee = validationParClasse.stream()
                .mapToLong(AvancementClasse::matieresAttendues).sum();

        // ─── À traiter, sur la période EN COURS (avec le détail salle + matière) ───
        Long etapeSoumiseId = etapeRepository.findByCode("SOUMISE").map(Etape::getId).orElse(null);
        Map<Long, String> classeLibelle = classes.stream()
                .collect(Collectors.toMap(Classe::getId,
                        c -> c.getLibelle() != null ? c.getLibelle() : c.getCode()));
        Map<Long, String> matiereLibelle = matiereRepository.findAll().stream()
                .collect(Collectors.toMap(Matiere::getId, Matiere::getLibelle));

        List<ProgressionSaisieNote> progPeriode = progressions.stream()
                .filter(p -> periode.getId().equals(p.getPeriodeId()))
                .toList();

        Set<String> soumisesOuValidees = progPeriode.stream()
                .filter(p -> p.getEtapeId() != null && etapesSoumises.contains(p.getEtapeId()))
                .map(p -> p.getClasseId() + "_" + p.getMatiereId())
                .collect(Collectors.toSet());

        List<MatiereATraiter> matieresATraiter = new java.util.ArrayList<>();

        // 1) Envoyées par les profs → à valider par l'administration
        for (ProgressionSaisieNote p : progPeriode) {
            if (p.getEtapeId() != null && etapeSoumiseId != null && etapeSoumiseId.equals(p.getEtapeId())) {
                matieresATraiter.add(new MatiereATraiter(
                        classeLibelle.getOrDefault(p.getClasseId(), "—"),
                        matiereLibelle.getOrDefault(p.getMatiereId(), "—"),
                        "A_VALIDER", false));
            }
        }

        // 2) Notes présentes mais pas encore envoyées → le professeur est en cours de saisie
        Set<String> vues = new java.util.HashSet<>();
        for (Object[] triplet : noteRepository.findTripletsAvecNotes(Set.of(periode.getId()))) {
            String cle = triplet[0] + "_" + triplet[1];
            if (soumisesOuValidees.contains(cle) || !vues.add(cle)) continue;

            matieresATraiter.add(new MatiereATraiter(
                    classeLibelle.getOrDefault((Long) triplet[0], "—"),
                    matiereLibelle.getOrDefault((Long) triplet[1], "—"),
                    "EN_SAISIE", false));
        }

        matieresATraiter.sort(Comparator
                .comparing(MatiereATraiter::etat)
                .thenComparing(MatiereATraiter::classeLibelle, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MatiereATraiter::matiereLibelle, Comparator.nullsLast(Comparator.naturalOrder())));

        long matieresAValider = matieresATraiter.stream().filter(m -> "A_VALIDER".equals(m.etat())).count();
        long matieresEnSaisie = matieresATraiter.stream().filter(m -> "EN_SAISIE".equals(m.etat())).count();

        List<ValidationBulletin> validations = validationBulletinRepository
                .findByPeriodeIdAndAnneeScolaireId(periode.getId(), anneeActive.getId());
        List<Long> classesValidees = validations.stream()
                .filter(v -> Boolean.TRUE.equals(v.getValide()))
                .map(ValidationBulletin::getClasseId)
                .distinct()
                .toList();

        long classesTotal = classes.stream()
                .filter(c -> c.getMatiereIds() != null && !c.getMatiereIds().isEmpty())
                .count();

        // Moyennes établissement : uniquement sur les classes dont le bulletin est validé (données figées).
        List<Double> moyennes = new java.util.ArrayList<>();
        for (Long classeId : classesValidees) {
            try {
                for (BulletinResponse b : bulletinService.genererBulletinsClasse(classeId, periode.getId())) {
                    if (b.getMoyennePonderee() != null) moyennes.add(b.getMoyennePonderee());
                }
            } catch (Exception e) {
                log.warn("[dashboard] bulletins classe {} période {} : {}", classeId, periode.getId(), e.getMessage());
            }
        }

        Double moyenneEtab = moyennes.isEmpty() ? null
                : moyennes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        Double tauxReussite = moyennes.isEmpty() ? null
                : 100.0 * moyennes.stream().filter(m -> m >= 10).count() / moyennes.size();

        List<Repartition> repartition = List.of(
                new Repartition("≥ 16", moyennes.stream().filter(m -> m >= 16).count()),
                new Repartition("14 – 16", moyennes.stream().filter(m -> m >= 14 && m < 16).count()),
                new Repartition("12 – 14", moyennes.stream().filter(m -> m >= 12 && m < 14).count()),
                new Repartition("10 – 12", moyennes.stream().filter(m -> m >= 10 && m < 12).count()),
                new Repartition("< 10", moyennes.stream().filter(m -> m < 10).count())
        );

        return new NotesBulletins(
                periode.getLibelle(),
                feuillesSoumises, feuillesAttendues,
                classesValidees.size(), classesTotal,
                moyenneEtab, tauxReussite, repartition,
                periodes.size(), matieresValideesAnnee, matieresAttenduesAnnee, validationParClasse,
                matieresAValider, matieresEnSaisie, matieresATraiter);
    }

    /** Vrai si {@code date} tombe dans [debut, fin] (bornes nulles = pas de contrainte). */
    private boolean dansPeriode(LocalDate date, LocalDate debut, LocalDate fin) {
        if (date == null) return debut == null && fin == null;
        if (debut != null && date.isBefore(debut)) return false;
        return fin == null || !date.isAfter(fin);
    }

    /** Répartit une liste sur les {@value NB_MOIS} derniers mois (mois vides = 0). */
    private <T> List<PointMensuel> serieMensuelle(List<T> items, Function<T, LocalDate> dateGetter,
                                                  ToDoubleFunction<T> valeur) {
        YearMonth courant = YearMonth.now();
        LinkedHashMap<YearMonth, Double> buckets = new LinkedHashMap<>();
        for (int i = NB_MOIS - 1; i >= 0; i--) {
            buckets.put(courant.minusMonths(i), 0.0);
        }
        for (T it : items) {
            LocalDate d = dateGetter.apply(it);
            if (d == null) continue;
            YearMonth ym = YearMonth.from(d);
            if (buckets.containsKey(ym)) {
                buckets.merge(ym, valeur.applyAsDouble(it), Double::sum);
            }
        }
        return buckets.entrySet().stream()
                .map(e -> new PointMensuel(e.getKey().toString(), e.getValue()))
                .toList();
    }
}
