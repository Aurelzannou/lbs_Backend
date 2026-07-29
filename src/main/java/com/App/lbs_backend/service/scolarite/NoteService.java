package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.FeuilleSaisieNotesRequest;
import com.App.lbs_backend.dto.response.ClasseMatiereANoterResponse;
import com.App.lbs_backend.dto.response.FeuilleSaisieNotesResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.Matiere;
import com.App.lbs_backend.entity.Note;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.EmploiDuTempsRepository;
import com.App.lbs_backend.repository.MatiereRepository;
import com.App.lbs_backend.repository.NoteRepository;
import com.App.lbs_backend.repository.PeriodeAcademiqueRepository;
import com.App.lbs_backend.repository.ValidationBulletinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private static final String INTERROGATION = "INTERROGATION";
    private static final String DEVOIR = "DEVOIR";

    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final NoteRepository noteRepository;
    private final ValidationBulletinRepository validationBulletinRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;

    /** Moyenne d'une matière = moyenne simple des valeurs non nulles parmi {interrogation,
        devoir1, devoir2} — formule vérifiée contre un bulletin réel de l'école. */
    public static Double calculerMoyenne(Double interrogation, Double devoir1, Double devoir2) {
        double somme = 0;
        int count = 0;
        if (interrogation != null) { somme += interrogation; count++; }
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

        List<Eleve> eleves = eleveRepository.findByClasseId(classeId);
        List<Long> eleveIds = eleves.stream().map(Eleve::getId).toList();

        Map<Long, List<Note>> notesParEleve = noteRepository
                .findByEleveIdInAndMatiereIdAndPeriodeId(eleveIds, matiereId, periodeId).stream()
                .collect(Collectors.groupingBy(Note::getEleveId));

        boolean valide = validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId)
                .map(v -> Boolean.TRUE.equals(v.getValide()))
                .orElse(false);

        FeuilleSaisieNotesResponse response = new FeuilleSaisieNotesResponse();
        response.setClasseId(classeId);
        response.setClasseLibelle(classe.getLibelle());
        response.setMatiereId(matiereId);
        response.setMatiereLibelle(matiere.getLibelle());
        response.setPeriodeId(periodeId);
        response.setPeriodeLibelle(periode.getLibelle());
        response.setValide(valide);

        response.setEleves(eleves.stream().map(el -> {
            List<Note> notes = notesParEleve.getOrDefault(el.getId(), List.of());
            Double interrogation = extraireValeur(notes, INTERROGATION, null);
            Double devoir1 = extraireValeur(notes, DEVOIR, 1);
            Double devoir2 = extraireValeur(notes, DEVOIR, 2);

            FeuilleSaisieNotesResponse.EleveNoteDto dto = new FeuilleSaisieNotesResponse.EleveNoteDto();
            dto.setEleveId(el.getId());
            dto.setNom(el.getNom());
            dto.setPrenom(el.getPrenom());
            dto.setInterrogation(interrogation);
            dto.setDevoir1(devoir1);
            dto.setDevoir2(devoir2);
            dto.setMoyenne(calculerMoyenne(interrogation, devoir1, devoir2));
            return dto;
        }).collect(Collectors.toList()));

        return response;
    }

    /** Extrait la valeur d'un type/numéro d'évaluation donné dans une liste de notes (réutilisé
        par BulletinService pour le calcul des bulletins). */
    public static Double extraireValeur(List<Note> notes, String type, Integer numeroDevoir) {
        return notes.stream()
                .filter(n -> type.equals(n.getTypeEvaluation()))
                .filter(n -> numeroDevoir == null
                        ? n.getNumeroDevoir() == null
                        : numeroDevoir.equals(n.getNumeroDevoir()))
                .findFirst()
                .map(Note::getValeur)
                .orElse(null);
    }

    /** Enregistre (upsert) la feuille de notes d'une classe/matière/période. */
    @Transactional
    public FeuilleSaisieNotesResponse enregistrerFeuille(FeuilleSaisieNotesRequest form) {
        verifierPeriodeNonValidee(form.getClasseId(), form.getPeriodeId());

        if (form.getProfesseurId() != null) {
            boolean autorise = emploiDuTempsRepository
                    .findDistinctClasseMatiereByProfIdAndAnneeScolaireId(
                            form.getProfesseurId(), resoudreAnneeScolaireId(form.getPeriodeId()))
                    .stream()
                    .anyMatch(p -> p.getClasseId().equals(form.getClasseId())
                            && p.getMatiereId().equals(form.getMatiereId()));
            if (!autorise) {
                throw new IllegalArgumentException("Vous n'êtes pas autorisé à noter cette classe/matière.");
            }
        }

        for (FeuilleSaisieNotesRequest.EleveNoteEntry entree : form.getEleves()) {
            upsertNote(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                    form.getProfesseurId(), INTERROGATION, null, entree.getInterrogation());
            upsertNote(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                    form.getProfesseurId(), DEVOIR, 1, entree.getDevoir1());
            upsertNote(entree.getEleveId(), form.getMatiereId(), form.getPeriodeId(),
                    form.getProfesseurId(), DEVOIR, 2, entree.getDevoir2());
        }

        return getFeuille(form.getClasseId(), form.getMatiereId(), form.getPeriodeId());
    }

    private void upsertNote(Long eleveId, Long matiereId, Long periodeId, Long professeurId,
                             String typeEvaluation, Integer numeroDevoir, Double valeur) {
        if (valeur == null) {
            noteRepository.findByEleveIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumeroDevoir(
                    eleveId, matiereId, periodeId, typeEvaluation, numeroDevoir)
                    .ifPresent(n -> { n.setValeur(null); noteRepository.save(n); });
            return;
        }

        Note note = noteRepository.findByEleveIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumeroDevoir(
                eleveId, matiereId, periodeId, typeEvaluation, numeroDevoir)
                .orElseGet(Note::new);
        note.setEleveId(eleveId);
        note.setMatiereId(matiereId);
        note.setPeriodeId(periodeId);
        note.setProfesseurId(professeurId);
        note.setTypeEvaluation(typeEvaluation);
        note.setNumeroDevoir(numeroDevoir);
        note.setValeur(valeur);
        if (note.getCode() == null) {
            note.setCode("NOT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        }
        noteRepository.save(note);
    }

    /** Couples (classe, matière) que ce professeur enseigne pour une année scolaire donnée. */
    public List<ClasseMatiereANoterResponse> getMesClassesANoter(Long profId, Long anneeScolaireId) {
        return emploiDuTempsRepository
                .findDistinctClasseMatiereByProfIdAndAnneeScolaireId(profId, anneeScolaireId).stream()
                .map(p -> {
                    ClasseMatiereANoterResponse dto = new ClasseMatiereANoterResponse();
                    dto.setClasseId(p.getClasseId());
                    dto.setMatiereId(p.getMatiereId());
                    classeRepository.findById(p.getClasseId()).ifPresent(c -> dto.setClasseLibelle(c.getLibelle()));
                    matiereRepository.findById(p.getMatiereId()).ifPresent(m -> dto.setMatiereLibelle(m.getLibelle()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private Long resoudreAnneeScolaireId(Long periodeId) {
        return periodeAcademiqueRepository.findById(periodeId)
                .map(PeriodeAcademique::getAnneeScolaireId)
                .orElseThrow(() -> new IllegalArgumentException("Période introuvable"));
    }
}
