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

    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final NoteRepository noteRepository;
    private final ValidationBulletinRepository validationBulletinRepository;
    private final ProfesseurRepository professeurRepository;
    private final ProgressionSaisieNoteRepository progressionSaisieNoteRepository;
    private final EtapeRepository etapeRepository;

    /** Moyenne des interrogations d'un élève pour une matière/période = Somme / Nombre des
        interrogations non nulles (le professeur peut en saisir autant qu'il veut). */
    public static Double calculerMoyenneInterrogations(List<Note> notes) {
        List<Double> valeurs = notes.stream()
                .filter(n -> INTERROGATION.equals(n.getTypeEvaluation()) && n.getValeur() != null)
                .map(Note::getValeur)
                .toList();
        if (valeurs.isEmpty()) return null;
        return valeurs.stream().mapToDouble(Double::doubleValue).sum() / valeurs.size();
    }

    /** Moyenne d'une matière = moyenne simple des valeurs non nulles parmi {moyenne des
        interrogations, devoir1, devoir2} — formule vérifiée contre un bulletin réel de l'école. */
    public static Double calculerMoyenne(Double moyenneInterrogations, Double devoir1, Double devoir2) {
        double somme = 0;
        int count = 0;
        if (moyenneInterrogations != null) { somme += moyenneInterrogations; count++; }
        if (devoir1 != null) { somme += devoir1; count++; }
        if (devoir2 != null) { somme += devoir2; count++; }
        return count == 0 ? null : somme / count;
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

        Map<Long, List<Note>> notesParEleve = noteRepository
                .findByEleveIdInAndMatiereIdAndPeriodeId(eleveIds, matiereId, periodeId).stream()
                .collect(Collectors.groupingBy(Note::getEleveId));

        boolean valide = validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId)
                .map(v -> Boolean.TRUE.equals(v.getValide()))
                .orElse(false);

        // Nombre de colonnes d'interrogation à afficher : le plus grand numéro déjà saisi pour
        // cette classe/matière/période, au moins 1 pour laisser une colonne de départ.
        Integer maxNumero = eleveIds.isEmpty() ? null
                : noteRepository.findMaxNumero(eleveIds, matiereId, periodeId, INTERROGATION);
        int nombreInterrogations = Math.min(maxNumero != null ? maxNumero : 1, MAX_INTERROGATIONS);

        FeuilleSaisieNotesResponse response = new FeuilleSaisieNotesResponse();
        response.setClasseId(classeId);
        response.setClasseLibelle(classe.getLibelle());
        response.setMatiereId(matiereId);
        response.setMatiereLibelle(matiere.getLibelle());
        response.setPeriodeId(periodeId);
        response.setPeriodeLibelle(periode.getLibelle());
        response.setValide(valide);
        response.setNombreInterrogations(nombreInterrogations);

        int nbInterro = nombreInterrogations;
        response.setEleves(eleves.stream().map(el -> {
            List<Note> notes = notesParEleve.getOrDefault(el.getId(), List.of());

            List<Double> interrogations = new ArrayList<>();
            for (int i = 1; i <= nbInterro; i++) {
                interrogations.add(extraireValeur(notes, INTERROGATION, i));
            }
            Double devoir1 = extraireValeur(notes, DEVOIR, 1);
            Double devoir2 = extraireValeur(notes, DEVOIR, 2);
            Double moyenneInterro = calculerMoyenneInterrogations(notes);

            FeuilleSaisieNotesResponse.EleveNoteDto dto = new FeuilleSaisieNotesResponse.EleveNoteDto();
            dto.setEleveId(el.getId());
            dto.setNom(el.getNom());
            dto.setPrenom(el.getPrenom());
            dto.setInterrogations(interrogations);
            dto.setDevoir1(devoir1);
            dto.setDevoir2(devoir2);
            dto.setMoyenneInterrogations(moyenneInterro);
            dto.setMoyenne(calculerMoyenne(moyenneInterro, devoir1, devoir2));
            return dto;
        }).collect(Collectors.toList()));

        return response;
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

    /** Enregistre (upsert) la feuille de notes d'une classe/matière/période. */
    @Transactional
    public FeuilleSaisieNotesResponse enregistrerFeuille(FeuilleSaisieNotesRequest form) {
        verifierPeriodeNonValidee(form.getClasseId(), form.getPeriodeId());
        verifierEtapeModifiable(form);

        if (form.getProfesseurId() != null) {
            Professeur professeur = professeurRepository.findById(form.getProfesseurId())
                    .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));
            boolean autorise = professeur.getClasseIds() != null && professeur.getClasseIds().contains(form.getClasseId())
                    && professeur.getMatiereIds() != null && professeur.getMatiereIds().contains(form.getMatiereId());
            if (!autorise) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à noter cette classe/matière.");
            }

            // Le professeur ne peut saisir que la période en cours (l'admin, lui, n'a pas cette
            // restriction — professeurId est null quand l'appel vient de l'écran admin).
            PeriodeAcademique periode = periodeAcademiqueRepository.findById(form.getPeriodeId())
                    .orElseThrow(() -> new IllegalArgumentException("Période introuvable"));
            if (!PeriodeAcademiqueMapper.EN_COURS.equals(PeriodeAcademiqueMapper.calculerStatut(periode))) {
                throw new IllegalArgumentException("Vous ne pouvez saisir des notes que pour la période en cours.");
            }

            // Colonne par colonne : le professeur ne peut pas modifier une colonne déjà verrouillée,
            // ni sauter directement à une colonne suivante sans avoir d'abord verrouillé la précédente.
            verifierColonnesModifiables(form);
        }

        // Le nombre de colonnes envoyées peut être inférieur à ce qui existait déjà en base (si le
        // professeur a réduit le nombre d'interrogations) — on va jusqu'au plus grand des deux pour
        // pouvoir vider (valeur=null) les numéros qui ne sont plus soumis, sans les perdre en silence.
        List<Long> eleveIds = form.getEleves().stream()
                .map(FeuilleSaisieNotesRequest.EleveNoteEntry::getEleveId).toList();
        Integer maxNumeroExistant = eleveIds.isEmpty() ? null
                : noteRepository.findMaxNumero(eleveIds, form.getMatiereId(), form.getPeriodeId(), INTERROGATION);
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
                upsertNote(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                        form.getProfesseurId(), INTERROGATION, i, valeur);
            }
            upsertNote(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                    form.getProfesseurId(), DEVOIR, 1, entree.getDevoir1());
            upsertNote(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                    form.getProfesseurId(), DEVOIR, 2, entree.getDevoir2());
        }

        return getFeuille(form.getClasseId(), form.getMatiereId(), form.getPeriodeId());
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
            if ("SOUMISE".equals(etape) || "VALIDEE".equals(etape)) {
                throw new IllegalArgumentException(
                        "Cette matière a déjà été soumise pour validation, vous ne pouvez plus la modifier.");
            }
        } else if ("VALIDEE".equals(etape)) {
            throw new IllegalArgumentException(
                    "Cette matière est déjà validée. Vous devez d'abord la dévalider pour la modifier.");
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

        for (FeuilleSaisieNotesRequest.EleveNoteEntry entree : form.getEleves()) {
            List<Double> interrogations = entree.getInterrogations() != null ? entree.getInterrogations() : List.of();
            for (int i = 1; i <= interrogations.size(); i++) {
                verifierColonneModifiable(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                        INTERROGATION, i, interrogations.get(i - 1), interroVerrouees);
            }
            verifierColonneModifiable(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                    DEVOIR, 1, entree.getDevoir1(), devoirsVerroues);
            verifierColonneModifiable(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                    DEVOIR, 2, entree.getDevoir2(), devoirsVerroues);
        }
    }

    private void verifierColonneModifiable(Long eleveId, Long matiereId, Long periodeId, String type,
                                            int numero, Double valeurSoumise, int verroueesJusqua) {
        if (numero > verroueesJusqua + 1) {
            String label = INTERROGATION.equals(type) ? "l'interrogation " + numero : "le devoir " + numero;
            throw new IllegalArgumentException(
                    "Vous devez d'abord terminer la colonne précédente avant de renseigner " + label + ".");
        }
        if (numero <= verroueesJusqua) {
            Double valeurActuelle = noteRepository.findByEleveIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
                    eleveId, matiereId, periodeId, type, numero).map(Note::getValeur).orElse(null);
            if (!Objects.equals(valeurActuelle, valeurSoumise)) {
                String label = INTERROGATION.equals(type) ? "L'interrogation " + numero : "Le devoir " + numero;
                throw new IllegalArgumentException(label + " est verrouillé(e) et ne peut plus être modifié(e).");
            }
        }
    }

    private void upsertNote(Long eleveId, Long matiereId, Long periodeId, Long professeurId,
                             String typeEvaluation, Integer numero, Double valeur) {
        if (valeur == null) {
            noteRepository.findByEleveIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
                    eleveId, matiereId, periodeId, typeEvaluation, numero)
                    .ifPresent(n -> { n.setValeur(null); noteRepository.save(n); });
            return;
        }

        Note note = noteRepository.findByEleveIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
                eleveId, matiereId, periodeId, typeEvaluation, numero)
                .orElseGet(Note::new);
        note.setEleveId(eleveId);
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
