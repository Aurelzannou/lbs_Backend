package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.FeuilleSaisieNotesRequest;
import com.App.lbs_backend.dto.response.ClasseMatiereANoterResponse;
import com.App.lbs_backend.dto.response.FeuilleSaisieNotesResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.Etape;
import com.App.lbs_backend.entity.Matiere;
import com.App.lbs_backend.entity.Note;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.entity.ProgressionSaisieNote;
import com.App.lbs_backend.mapper.PeriodeAcademiqueMapper;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.EtapeRepository;
import com.App.lbs_backend.repository.MatiereRepository;
import com.App.lbs_backend.repository.NoteRepository;
import com.App.lbs_backend.repository.PeriodeAcademiqueRepository;
import com.App.lbs_backend.repository.PresenceEleveRepository;
import com.App.lbs_backend.repository.ProfesseurRepository;
import com.App.lbs_backend.repository.ProgressionSaisieNoteRepository;
import com.App.lbs_backend.repository.ValidationBulletinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    public static final String INTERROGATION = "INTERROGATION";
    public static final String DEVOIR = "DEVOIR";
    public static final int MAX_INTERROGATIONS = 4;
    public static final int MAX_DEVOIRS = 2;

    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final NoteRepository noteRepository;
    private final ValidationBulletinRepository validationBulletinRepository;
    private final ProfesseurRepository professeurRepository;
    private final ProgressionSaisieNoteRepository progressionSaisieNoteRepository;
    private final EtapeRepository etapeRepository;
    private final PresenceEleveRepository presenceEleveRepository;
    private final ProgressionSaisieNoteService progressionSaisieNoteService;

    /** Base de la suggestion automatique de conduite : 18, moins 1 point par absence enregistrée
        sur la période (jamais persistée tant que l'admin n'a pas enregistré la feuille). */
    private static final double CONDUITE_BASE = 18.0;

    /** Nombre d'évaluations d'un type réellement organisées pour la classe = le plus grand numéro
        présent dans les notes de la classe (borné). Sert de dénominateur des moyennes : une
        évaluation prévue mais non composée par l'élève compte 0. */
    public static int nombreEvaluationsOrganisees(List<Note> notesClasseMatiere, String type, int max) {
        int n = notesClasseMatiere.stream()
                .filter(x -> type.equals(x.getTypeEvaluation()) && x.getNumero() != null && x.getValeur() != null)
                .mapToInt(Note::getNumero)
                .max().orElse(0);
        return Math.min(n, max);
    }

    /** Moyenne d'une composante (interrogations OU devoirs) pour un élève =
        somme de ses notes (0 pour chaque évaluation organisée mais non composée)
        ÷ nombre d'évaluations organisées pour la classe. null si aucune n'a été organisée. */
    public static Double moyenneComposante(List<Note> notesEleve, String type, int nombreOrganise) {
        if (nombreOrganise <= 0) return null;
        double somme = 0;
        for (int i = 1; i <= nombreOrganise; i++) {
            Double v = extraireValeur(notesEleve, type, i);
            somme += v != null ? v : 0.0;
        }
        return somme / nombreOrganise;
    }

    /** Moyenne des interrogations = somme des interros de l'élève (0 pour une interro non composée)
        ÷ nombre d'interros organisées pour la classe. Conservée pour l'affichage de la colonne
        "Interro" du bulletin et de la feuille de saisie. */
    public static Double calculerMoyenneInterrogations(List<Note> notesEleve, int nombreInterrosOrganisees) {
        return moyenneComposante(notesEleve, INTERROGATION, nombreInterrosOrganisees);
    }

    /** Moyenne d'une matière = (moyenne des interrogations + moyenne des devoirs) ÷ 2.
        - Chaque évaluation organisée mais non composée compte 0 (cf. moyenneComposante).
        - Si une seule des deux composantes a été organisée, elle vaut à elle seule la moyenne.
        - Matière jamais évaluée : moyenne = 0 (elle reste comptée, jamais simplement exclue).
        - Conduite : une seule note, sa valeur telle quelle (0 si non notée). */
    public static Double calculerMoyenneMatiere(Double moyenneInterrogations, Double moyenneDevoirs, boolean estConduite) {
        if (estConduite) return moyenneInterrogations != null ? moyenneInterrogations : 0.0;
        if (moyenneInterrogations == null && moyenneDevoirs == null) return 0.0;
        if (moyenneInterrogations == null) return moyenneDevoirs;
        if (moyenneDevoirs == null) return moyenneInterrogations;
        return (moyenneInterrogations + moyenneDevoirs) / 2.0;
    }

    /** Bloque toute saisie/modification de notes si le bulletin de cette classe/période est déjà
        validé — même esprit que EmploiDuTempsService.verifierAnneeModifiable. */
    public void verifierPeriodeNonValidee(Long classeId, Long periodeId) {
        validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId)
                .filter(v -> Boolean.TRUE.equals(v.getValide()))
                .ifPresent(v -> {
                    throw new IllegalArgumentException(
                            "Le bulletin de cette classe pour cette période est déjà validé : les notes ne peuvent plus être modifiées.");
                });
    }

    /** Charge la feuille de saisie (roster + notes déjà saisies) pour une classe/matière/période. */
    public FeuilleSaisieNotesResponse getFeuille(Long classeId, Long matiereId, Long periodeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new IllegalArgumentException("Classe introuvable"));
        Matiere matiere = matiereRepository.findById(matiereId)
                .orElseThrow(() -> new IllegalArgumentException("Matière introuvable"));
        PeriodeAcademique periode = periodeAcademiqueRepository.findById(periodeId)
                .orElseThrow(() -> new IllegalArgumentException("Période introuvable"));

        List<Eleve> eleves = eleveRepository.findByClasseIdOrderByNomAscPrenomAsc(classeId);
        List<Long> eleveIds = eleves.stream().map(Eleve::getId).toList();

        List<Note> toutesLesNotes = noteRepository
                .findByEleveIdInAndMatiereIdAndPeriodeIdAndClasseId(eleveIds, matiereId, periodeId, classeId);
        Map<Long, List<Note>> notesParEleve = toutesLesNotes.stream()
                .collect(Collectors.groupingBy(Note::getEleveId));

        boolean valide = validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId)
                .map(v -> Boolean.TRUE.equals(v.getValide()))
                .orElse(false);

        boolean estConduite = Boolean.TRUE.equals(matiere.getEstConduite());

        // Nombre de colonnes d'interrogation à afficher : le plus grand numéro déjà saisi pour
        // cette classe/matière/période, au moins 1 pour laisser une colonne de départ.
        Integer maxNumero = eleveIds.isEmpty() ? null
                : noteRepository.findMaxNumero(eleveIds, classeId, matiereId, periodeId, INTERROGATION);
        int nombreInterrogations = Math.min(maxNumero != null ? maxNumero : 1, MAX_INTERROGATIONS);

        // Dénominateurs des moyennes : nombre d'évaluations réellement organisées pour la classe
        // (une évaluation prévue mais non composée par l'élève comptera 0).
        int nbInterrosOrganisees = estConduite ? 1
                : nombreEvaluationsOrganisees(toutesLesNotes, INTERROGATION, MAX_INTERROGATIONS);
        int nbDevoirsOrganises = estConduite ? 0
                : nombreEvaluationsOrganisees(toutesLesNotes, DEVOIR, MAX_DEVOIRS);

        FeuilleSaisieNotesResponse response = new FeuilleSaisieNotesResponse();
        response.setClasseId(classeId);
        response.setClasseLibelle(classe.getLibelle());
        response.setMatiereId(matiereId);
        response.setMatiereLibelle(matiere.getLibelle());
        response.setPeriodeId(periodeId);
        response.setPeriodeLibelle(periode.getLibelle());
        response.setValide(valide);
        response.setNombreInterrogations(nombreInterrogations);
        response.setEstConduite(estConduite);
        response.setProfesseurAssigne(professeurRepository.findAll().stream().anyMatch(p ->
                p.getClasseIds() != null && p.getClasseIds().contains(classeId)
                        && p.getMatiereIds() != null && p.getMatiereIds().contains(matiereId)));

        int nbInterro = nombreInterrogations;
        response.setEleves(eleves.stream().map(el -> {
            List<Note> notes = notesParEleve.getOrDefault(el.getId(), List.of());

            List<Double> interrogations = new ArrayList<>();
            for (int i = 1; i <= nbInterro; i++) {
                interrogations.add(extraireValeur(notes, INTERROGATION, i));
            }
            Double devoir1 = extraireValeur(notes, DEVOIR, 1);
            Double devoir2 = extraireValeur(notes, DEVOIR, 2);
            Double moyenneInterro = calculerMoyenneInterrogations(notes, nbInterrosOrganisees);
            Double moyenneDevoirs = estConduite ? null
                    : moyenneComposante(notes, DEVOIR, nbDevoirsOrganises);

            // La matière "Conduite" n'a qu'une seule valeur (colonne Interrogation 1) — tant
            // qu'aucune note n'a encore été saisie pour cet élève, on propose une suggestion basée
            // sur ses absences (18 - nombre d'absences), jamais persistée avant enregistrement.
            if (estConduite && !interrogations.isEmpty() && interrogations.get(0) == null) {
                Double suggestion = suggererConduite(el.getId(), periode);
                interrogations.set(0, suggestion);
                moyenneInterro = suggestion;
            }

            FeuilleSaisieNotesResponse.EleveNoteDto dto = new FeuilleSaisieNotesResponse.EleveNoteDto();
            dto.setEleveId(el.getId());
            dto.setNom(el.getNom());
            dto.setPrenom(el.getPrenom());
            dto.setInterrogations(interrogations);
            dto.setDevoir1(devoir1);
            dto.setDevoir2(devoir2);
            dto.setMoyenneInterrogations(moyenneInterro);
            dto.setMoyenne(calculerMoyenneMatiere(moyenneInterro, moyenneDevoirs, estConduite));
            return dto;
        }).collect(Collectors.toList()));

        return response;
    }

    /** Suggestion automatique de conduite : 18 points de base, moins 1 point par absence
        enregistrée sur l'intervalle de dates de la période — proposée à l'admin, jamais imposée. */
    private Double suggererConduite(Long eleveId, PeriodeAcademique periode) {
        if (periode.getDateDebut() == null || periode.getDateFin() == null) return CONDUITE_BASE;
        long absences = presenceEleveRepository.countByEleveIdAndDateBetweenAndStatut(
                eleveId, periode.getDateDebut(), periode.getDateFin(), "ABSENT");
        return Math.max(0, CONDUITE_BASE - absences);
    }

    /** Extrait la valeur d'un type/numéro d'évaluation donné dans une liste de notes (réutilisé
        par BulletinService pour le calcul des bulletins). */
    public static Double extraireValeur(List<Note> notes, String type, Integer numero) {
        return notes.stream()
                .filter(n -> type.equals(n.getTypeEvaluation()))
                .filter(n -> numero == null ? n.getNumero() == null : numero.equals(n.getNumero()))
                .findFirst()
                .map(Note::getValeur)
                .orElse(null);
    }

    /** Enregistre (upsert) la feuille de notes d'une classe/matière/période — point d'entrée du
        service de SAISIE (portail professeur + écran admin "Saisie des notes"). La correction
        depuis l'écran "Validation des bulletins" est un service à part entière, voir
        {@link #enregistrerFeuilleCorrection} et {@code ValidationBulletinService.corrigerNotes} —
        elle ne transite jamais par cette méthode, pour qu'aucune restriction de l'une ne puisse
        fuiter (ou manquer) sur l'autre. */
    @Transactional
    public FeuilleSaisieNotesResponse enregistrerFeuille(FeuilleSaisieNotesRequest form) {
        verifierRestrictionsUniverselles(form);

        if (form.getProfesseurId() != null) {
            enregistrerFeuilleProfesseur(form);
        } else {
            enregistrerFeuilleSaisieAdmin(form);
        }

        return getFeuille(form.getClasseId(), form.getMatiereId(), form.getPeriodeId());
    }

    private void verifierRestrictionsUniverselles(FeuilleSaisieNotesRequest form) {
        verifierPeriodeNonValidee(form.getClasseId(), form.getPeriodeId());
        verifierEtapeModifiable(form);
        // Une colonne déjà validée par l'admin est figée pour tout le monde, y compris l'admin lui-
        // même — la seule façon d'y retoucher est de d'abord "Déverrouiller" cette colonne (ce qui
        // annule aussi sa validation).
        verifierColonnesNonValidees(form);
    }

    /** Service de saisie professeur (portail prof) : limité à sa/ses classe(s)+matière(s)
        assignées, à la période EN_COURS d'une année active, colonne par colonne dans l'ordre. */
    private void enregistrerFeuilleProfesseur(FeuilleSaisieNotesRequest form) {
        Professeur professeur = professeurRepository.findById(form.getProfesseurId())
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));
        boolean autorise = professeur.getClasseIds() != null && professeur.getClasseIds().contains(form.getClasseId())
                && professeur.getMatiereIds() != null && professeur.getMatiereIds().contains(form.getMatiereId());
        if (!autorise) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à noter cette classe/matière.");
        }

        PeriodeAcademique periode = periodeAcademiqueRepository.findById(form.getPeriodeId())
                .orElseThrow(() -> new IllegalArgumentException("Période introuvable"));
        if (!PeriodeAcademiqueMapper.EN_COURS.equals(PeriodeAcademiqueMapper.calculerStatut(periode))) {
            throw new IllegalArgumentException("Vous ne pouvez saisir des notes que pour la période en cours.");
        }
        // Le statut EN_COURS n'est calculé que sur les dates de la période — il ne suffit pas :
        // si l'admin a désactivé l'année scolaire (par erreur, fin d'année, etc.), la saisie doit
        // être bloquée même si la période tombe encore dans son intervalle de dates.
        if (periode.getAnneeScolaire() == null || !Boolean.TRUE.equals(periode.getAnneeScolaire().getActif())) {
            throw new IllegalArgumentException("L'année scolaire de cette période n'est plus active.");
        }

        // Colonne par colonne : le professeur ne peut pas modifier une colonne déjà verrouillée,
        // ni sauter directement à une colonne suivante sans avoir d'abord verrouillé la précédente.
        verifierColonnesModifiables(form);
        ecrireNotes(form);
        // Modifier / compléter une matière déjà envoyée annule l'envoi : elle repasse en brouillon
        // et l'enseignant devra la renvoyer une fois terminé.
        progressionSaisieNoteService.reprendreSiEnvoyee(form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                professeur.getEmail() + " (modification après envoi)");
    }

    /** Service de saisie directe admin (écran "Saisie des notes") : mêmes restrictions de période
        que le professeur (EN_COURS + année active) — c'est un écran de saisie du quotidien, pas de
        correction historique — mais sans vérification d'autorisation ni d'ordre des colonnes,
        l'admin pouvant toujours saisir librement dans n'importe quel ordre. */
    private void enregistrerFeuilleSaisieAdmin(FeuilleSaisieNotesRequest form) {
        PeriodeAcademique periode = periodeAcademiqueRepository.findById(form.getPeriodeId())
                .orElseThrow(() -> new IllegalArgumentException("Période introuvable"));
        if (periode.getAnneeScolaire() == null || !Boolean.TRUE.equals(periode.getAnneeScolaire().getActif())) {
            throw new IllegalArgumentException(
                    "Cette période appartient à une année scolaire inactive. Utilisez l'écran de validation des bulletins pour la modifier.");
        }
        if (!PeriodeAcademiqueMapper.EN_COURS.equals(PeriodeAcademiqueMapper.calculerStatut(periode))) {
            throw new IllegalArgumentException(
                    "Vous ne pouvez saisir des notes que pour la période en cours depuis cet écran. Utilisez l'écran de validation des bulletins pour une autre période.");
        }
        ecrireNotes(form);
    }

    /** Service de correction, appelé exclusivement par {@code ValidationBulletinService.corrigerNotes}
        (écran "Validation des bulletins") — jamais directement par le contrôleur de saisie.
        Aucune restriction de période, d'étape ou de colonne : l'admin doit pouvoir corriger
        n'importe quelle note à tout moment depuis cet écran. Seul le verrou de classe
        ({@link #verifierPeriodeNonValidee}) s'applique encore — c'est le seul verrou piloté par
        cet écran lui-même (bouton Valider/Dévalider les bulletins de la classe) ; le dévalider
        suffit alors à débloquer de nouveau la correction. */
    @Transactional
    public FeuilleSaisieNotesResponse enregistrerFeuilleCorrection(FeuilleSaisieNotesRequest form) {
        verifierPeriodeNonValidee(form.getClasseId(), form.getPeriodeId());
        ecrireNotes(form);
        return getFeuille(form.getClasseId(), form.getMatiereId(), form.getPeriodeId());
    }

    private void ecrireNotes(FeuilleSaisieNotesRequest form) {
        // Le nombre de colonnes envoyées peut être inférieur à ce qui existait déjà en base (si le
        // professeur a réduit le nombre d'interrogations) — on va jusqu'au plus grand des deux pour
        // pouvoir vider (valeur=null) les numéros qui ne sont plus soumis, sans les perdre en silence.
        List<Long> eleveIds = form.getEleves().stream()
                .map(FeuilleSaisieNotesRequest.EleveNoteEntry::getEleveId).toList();
        Integer maxNumeroExistant = eleveIds.isEmpty() ? null
                : noteRepository.findMaxNumero(eleveIds, form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), INTERROGATION);
        int maxNumeroSoumis = form.getEleves().stream()
                .map(e -> e.getInterrogations() == null ? 0 : e.getInterrogations().size())
                .max(Integer::compareTo).orElse(0);
        int nombreInterrogations = Math.min(
                Math.max(maxNumeroExistant != null ? maxNumeroExistant : 0, maxNumeroSoumis),
                MAX_INTERROGATIONS);

        for (FeuilleSaisieNotesRequest.EleveNoteEntry entree : form.getEleves()) {
            List<Double> interrogations = entree.getInterrogations() != null ? entree.getInterrogations() : List.of();
            for (int i = 1; i <= nombreInterrogations; i++) {
                Double valeur = i <= interrogations.size() ? interrogations.get(i - 1) : null;
                upsertNote(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                        form.getProfesseurId(), INTERROGATION, i, valeur);
            }
            upsertNote(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                    form.getProfesseurId(), DEVOIR, 1, entree.getDevoir1());
            upsertNote(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                    form.getProfesseurId(), DEVOIR, 2, entree.getDevoir2());
        }
    }

    /** Le workflow de validation (étape) bloque l'écriture indépendamment du verrou colonne par
        colonne : le professeur ne peut plus rien modifier une fois la matière soumise ou validée ;
        l'admin, lui, garde la main jusqu'à la validation, puis doit dévalider pour continuer. */
    private void verifierEtapeModifiable(FeuilleSaisieNotesRequest form) {
        ProgressionSaisieNote progression = progressionSaisieNoteRepository
                .findByClasseIdAndMatiereIdAndPeriodeId(form.getClasseId(), form.getMatiereId(), form.getPeriodeId())
                .orElse(null);
        if (progression == null || progression.getEtapeId() == null) return;
        String etape = etapeRepository.findById(progression.getEtapeId()).map(Etape::getCode).orElse(null);
        if (form.getProfesseurId() != null) {
            // Scénario simplifié : l'enseignant garde la main tant que ce n'est pas VALIDÉ. S'il
            // modifie une matière déjà envoyée (SOUMISE), l'envoi est simplement annulé
            // (voir enregistrerFeuilleProfesseur → reprendreSiEnvoyee) et il devra la renvoyer.
            if ("VALIDEE".equals(etape)) {
                throw new IllegalArgumentException(
                        "Cette matière est validée par l'administration, vous ne pouvez plus la modifier.");
            }
        } else if ("VALIDEE".equals(etape)) {
            throw new IllegalArgumentException(
                    "Cette matière est déjà validée. Vous devez d'abord la dévalider pour la modifier.");
        }
    }

    private void verifierColonnesNonValidees(FeuilleSaisieNotesRequest form) {
        ProgressionSaisieNote progression = progressionSaisieNoteRepository
                .findByClasseIdAndMatiereIdAndPeriodeId(form.getClasseId(), form.getMatiereId(), form.getPeriodeId())
                .orElse(null);
        int interroValidees = progression != null && progression.getInterrogationsValideesJusqua() != null
                ? progression.getInterrogationsValideesJusqua() : 0;
        int devoirsValidees = progression != null && progression.getDevoirsValideesJusqua() != null
                ? progression.getDevoirsValideesJusqua() : 0;
        if (interroValidees == 0 && devoirsValidees == 0) return;

        for (FeuilleSaisieNotesRequest.EleveNoteEntry entree : form.getEleves()) {
            List<Double> interrogations = entree.getInterrogations() != null ? entree.getInterrogations() : List.of();
            for (int i = 1; i <= interrogations.size() && i <= interroValidees; i++) {
                verifierValeurInchangee(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                        INTERROGATION, i, interrogations.get(i - 1));
            }
            if (devoirsValidees >= 1) {
                verifierValeurInchangee(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                        DEVOIR, 1, entree.getDevoir1());
            }
            if (devoirsValidees >= 2) {
                verifierValeurInchangee(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                        DEVOIR, 2, entree.getDevoir2());
            }
        }
    }

    private void verifierValeurInchangee(Long eleveId, Long classeId, Long matiereId, Long periodeId, String type,
                                          int numero, Double valeurSoumise) {
        Double valeurActuelle = noteRepository.findByEleveIdAndClasseIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
                eleveId, classeId, matiereId, periodeId, type, numero).map(Note::getValeur).orElse(null);
        if (!Objects.equals(valeurActuelle, valeurSoumise)) {
            String label = INTERROGATION.equals(type) ? "L'interrogation " + numero : "Le devoir " + numero;
            throw new IllegalArgumentException(
                    label + " est validé(e) par l'administration et ne peut plus être modifié(e) (déverrouiller d'abord si besoin).");
        }
    }

    private void verifierColonnesModifiables(FeuilleSaisieNotesRequest form) {
        ProgressionSaisieNote progression = progressionSaisieNoteRepository
                .findByClasseIdAndMatiereIdAndPeriodeId(form.getClasseId(), form.getMatiereId(), form.getPeriodeId())
                .orElse(null);
        int interroVerrouees = progression != null && progression.getInterrogationsVerroueesJusqua() != null
                ? progression.getInterrogationsVerroueesJusqua() : 0;
        int devoirsVerroues = progression != null && progression.getDevoirsVerrouesJusqua() != null
                ? progression.getDevoirsVerrouesJusqua() : 0;
        int interroValidees = progression != null && progression.getInterrogationsValideesJusqua() != null
                ? progression.getInterrogationsValideesJusqua() : 0;
        int devoirsValidees = progression != null && progression.getDevoirsValideesJusqua() != null
                ? progression.getDevoirsValideesJusqua() : 0;

        for (FeuilleSaisieNotesRequest.EleveNoteEntry entree : form.getEleves()) {
            List<Double> interrogations = entree.getInterrogations() != null ? entree.getInterrogations() : List.of();
            for (int i = 1; i <= interrogations.size(); i++) {
                verifierColonneModifiable(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                        INTERROGATION, i, interrogations.get(i - 1), interroVerrouees, interroValidees);
            }
            verifierColonneModifiable(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                    DEVOIR, 1, entree.getDevoir1(), devoirsVerroues, devoirsValidees);
            verifierColonneModifiable(entree.getEleveId(), form.getClasseId(), form.getMatiereId(), form.getPeriodeId(),
                    DEVOIR, 2, entree.getDevoir2(), devoirsVerroues, devoirsValidees);
        }
    }

    private void verifierColonneModifiable(Long eleveId, Long classeId, Long matiereId, Long periodeId, String type,
                                            int numero, Double valeurSoumise, int verroueesJusqua, int valideesJusqua) {
        // Scénario simplifié : une colonne est modifiable tant qu'elle n'a pas été figée par un
        // envoi à l'administration. Les colonnes 1..verroueesJusqua sont figées ; les suivantes,
        // que l'enseignant vient d'ajouter, restent librement saisissables (dans n'importe quel
        // ordre). Seule une VRAIE tentative de modification d'une colonne figée est refusée — le
        // formulaire renvoie toujours l'état complet de la feuille, colonnes figées comprises.
        if (numero <= verroueesJusqua && aChange(eleveId, classeId, matiereId, periodeId, type, numero, valeurSoumise)) {
            String label = INTERROGATION.equals(type) ? "L'interrogation " + numero : "Le devoir " + numero;
            throw new IllegalArgumentException(
                    label + " a déjà été envoyé(e) à l'administration et ne peut plus être modifié(e).");
        }
    }

    private boolean aChange(Long eleveId, Long classeId, Long matiereId, Long periodeId, String type, int numero, Double valeurSoumise) {
        Double valeurActuelle = noteRepository.findByEleveIdAndClasseIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
                eleveId, classeId, matiereId, periodeId, type, numero).map(Note::getValeur).orElse(null);
        return !Objects.equals(valeurActuelle, valeurSoumise);
    }

    private void upsertNote(Long eleveId, Long classeId, Long matiereId, Long periodeId, Long professeurId,
                             String typeEvaluation, Integer numero, Double valeur) {
        if (valeur == null) {
            noteRepository.findByEleveIdAndClasseIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
                    eleveId, classeId, matiereId, periodeId, typeEvaluation, numero)
                    .ifPresent(n -> { n.setValeur(null); noteRepository.save(n); });
            return;
        }

        Note note = noteRepository.findByEleveIdAndClasseIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
                eleveId, classeId, matiereId, periodeId, typeEvaluation, numero)
                .orElseGet(Note::new);
        note.setEleveId(eleveId);
        note.setClasseId(classeId);
        note.setMatiereId(matiereId);
        note.setPeriodeId(periodeId);
        note.setProfesseurId(professeurId);
        note.setTypeEvaluation(typeEvaluation);
        note.setNumero(numero);
        note.setValeur(Math.min(20.0, Math.max(0.0, valeur)));
        if (note.getCode() == null) {
            note.setCode("NOT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        }
        noteRepository.save(note);
    }

    /** Couples (classe, matière) que ce professeur peut noter — produit du produit cartésien de
        ses classes affectées et de ses matières enseignées (affectation explicite, indépendante
        de l'emploi du temps : le professeur peut saisir des notes à tout moment). */
    public List<ClasseMatiereANoterResponse> getMesClassesANoter(Long profId) {
        Professeur professeur = professeurRepository.findById(profId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));

        List<Long> classeIds = professeur.getClasseIds() != null ? professeur.getClasseIds() : List.of();
        List<Long> matiereIds = professeur.getMatiereIds() != null ? professeur.getMatiereIds() : List.of();

        List<ClasseMatiereANoterResponse> resultat = new ArrayList<>();
        for (Long classeId : classeIds) {
            String classeLibelle = classeRepository.findById(classeId).map(Classe::getLibelle).orElse(null);
            for (Long matiereId : matiereIds) {
                ClasseMatiereANoterResponse dto = new ClasseMatiereANoterResponse();
                dto.setClasseId(classeId);
                dto.setClasseLibelle(classeLibelle);
                dto.setMatiereId(matiereId);
                matiereRepository.findById(matiereId).ifPresent(m -> dto.setMatiereLibelle(m.getLibelle()));
                resultat.add(dto);
            }
        }
        return resultat;
    }
}
