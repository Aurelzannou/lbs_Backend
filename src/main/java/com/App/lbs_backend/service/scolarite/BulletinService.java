package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.BulletinMentionRequest;
import com.App.lbs_backend.dto.response.BulletinMatiereResponse;
import com.App.lbs_backend.dto.response.BulletinResponse;
import com.App.lbs_backend.entity.BulletinMention;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.Coefficient;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.Note;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.repository.BulletinMentionRepository;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.CoefficientRepository;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.MatiereRepository;
import com.App.lbs_backend.repository.NoteRepository;
import com.App.lbs_backend.repository.PeriodeAcademiqueRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.repository.ValidationBulletinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Calcul et lecture des bulletins — service de reporting pur, pas de CRUD générique. Toutes les
    formules ont été vérifiées chiffre par chiffre contre un bulletin réel de l'école. */
@Service
@RequiredArgsConstructor
public class BulletinService {

    private final EleveRepository eleveRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final CoefficientRepository coefficientRepository;
    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final NoteRepository noteRepository;
    private final BulletinMentionRepository bulletinMentionRepository;
    private final ValidationBulletinRepository validationBulletinRepository;
    private final TuteurRepository tuteurRepository;

    public BulletinResponse genererBulletin(Long eleveId, Long periodeId) {
        Eleve eleve = eleveRepository.findById(eleveId)
                .orElseThrow(() -> new IllegalArgumentException("Élève introuvable"));
        return genererBulletinsClasse(eleve.getClasseId(), periodeId).stream()
                .filter(b -> b.getEleveId().equals(eleveId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Bulletin introuvable"));
    }

    /** Bulletin d'un élève pour son tuteur — refusé tant que la période n'est pas validée pour la
        classe de l'enfant. */
    public BulletinResponse genererBulletinPourTuteur(Long eleveId, Long periodeId, String email) {
        Tuteur tuteur = tuteurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tuteur introuvable"));
        Eleve eleve = eleveRepository.findById(eleveId)
                .orElseThrow(() -> new IllegalArgumentException("Élève introuvable"));
        if (!tuteur.getId().equals(eleve.getTuteurId())) {
            throw new IllegalArgumentException("Accès refusé.");
        }
        boolean valide = validationBulletinRepository.findByClasseIdAndPeriodeId(eleve.getClasseId(), periodeId)
                .map(v -> Boolean.TRUE.equals(v.getValide()))
                .orElse(false);
        if (!valide) {
            throw new IllegalArgumentException("Le bulletin de cette période n'est pas encore disponible.");
        }
        return genererBulletin(eleveId, periodeId);
    }

    public List<BulletinResponse> genererBulletinsClasse(Long classeId, Long periodeId) {
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new IllegalArgumentException("Classe introuvable"));
        PeriodeAcademique periode = periodeAcademiqueRepository.findById(periodeId)
                .orElseThrow(() -> new IllegalArgumentException("Période introuvable"));

        List<Eleve> eleves = eleveRepository.findByClasseIdOrderByNomAscPrenomAsc(classeId);
        List<Long> eleveIds = eleves.stream().map(Eleve::getId).toList();

        Map<Long, Double> coefficientsParMatiere = classe.getNiveauId() == null
                ? Map.of()
                : coefficientRepository.findByNiveauId(classe.getNiveauId()).stream()
                    .collect(Collectors.toMap(Coefficient::getMatiereId, Coefficient::getValeur));

        List<Note> notes = noteRepository.findByEleveIdInAndPeriodeId(eleveIds, periodeId);

        Map<Long, Map<Long, List<Note>>> parMatiereParEleve = notes.stream()
                .collect(Collectors.groupingBy(Note::getMatiereId, Collectors.groupingBy(Note::getEleveId)));

        Map<Long, Map<Long, Double>> moyenneParMatiereParEleve = new HashMap<>();
        for (Map.Entry<Long, Map<Long, List<Note>>> matiereEntry : parMatiereParEleve.entrySet()) {
            Map<Long, Double> parEleve = new HashMap<>();
            for (Map.Entry<Long, List<Note>> eleveEntry : matiereEntry.getValue().entrySet()) {
                parEleve.put(eleveEntry.getKey(), extraireMoyenne(eleveEntry.getValue()));
            }
            moyenneParMatiereParEleve.put(matiereEntry.getKey(), parEleve);
        }

        Map<Long, Map<Long, Integer>> rangParMatiereParEleve = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> matiereEntry : moyenneParMatiereParEleve.entrySet()) {
            rangParMatiereParEleve.put(matiereEntry.getKey(), computeRanks(matiereEntry.getValue()));
        }

        Map<Long, String> matiereLibelles = new HashMap<>();
        for (Long matiereId : parMatiereParEleve.keySet()) {
            matiereRepository.findById(matiereId).ifPresent(m -> matiereLibelles.put(matiereId, m.getLibelle()));
        }

        Map<Long, Double> moyennePondereeParEleve = calculerMoyennesPonderees(
                eleveIds, moyenneParMatiereParEleve, coefficientsParMatiere);
        Map<Long, Integer> rangTrimestreParEleve = computeRanks(moyennePondereeParEleve);

        List<PeriodeAcademique> periodesAnnee = periode.getAnneeScolaireId() == null
                ? List.of()
                : periodeAcademiqueRepository.findByAnneeScolaireIdOrderByDateDebutAsc(periode.getAnneeScolaireId());

        Map<Long, List<Double>> moyennesAnnuelles = new HashMap<>();
        for (PeriodeAcademique p : periodesAnnee) {
            Map<Long, Double> moyennesPeriode = p.getId().equals(periodeId)
                    ? moyennePondereeParEleve
                    : moyennesPartiellesParEleve(p.getId(), eleveIds, coefficientsParMatiere);
            for (Long eleveId : eleveIds) {
                Double v = moyennesPeriode.get(eleveId);
                if (v != null) moyennesAnnuelles.computeIfAbsent(eleveId, k -> new ArrayList<>()).add(v);
            }
        }
        Map<Long, Double> moyenneAnnuelleParEleve = new HashMap<>();
        for (Long eleveId : eleveIds) {
            List<Double> vals = moyennesAnnuelles.get(eleveId);
            moyenneAnnuelleParEleve.put(eleveId,
                    (vals == null || vals.isEmpty()) ? null : vals.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        }
        Map<Long, Integer> rangAnnuelParEleve = computeRanks(moyenneAnnuelleParEleve);

        List<BulletinResponse> resultats = new ArrayList<>();
        for (Eleve eleve : eleves) {
            BulletinResponse r = new BulletinResponse();
            r.setEleveId(eleve.getId());
            r.setEleveNomComplet(eleve.getNom() + " " + eleve.getPrenom());
            r.setClasseId(classeId);
            r.setClasseLibelle(classe.getLibelle());
            r.setEffectifClasse(eleves.size());
            r.setPeriodeId(periodeId);
            r.setPeriodeLibelle(periode.getLibelle());
            if (periode.getAnneeScolaire() != null) r.setAnneeScolaireLibelle(periode.getAnneeScolaire().getLibelle());

            List<BulletinMatiereResponse> matieres = new ArrayList<>();
            for (Long matiereId : parMatiereParEleve.keySet()) {
                Double moyenne = moyenneParMatiereParEleve.get(matiereId).get(eleve.getId());
                if (moyenne == null) continue;

                List<Note> notesEleveMatiere = parMatiereParEleve.get(matiereId).getOrDefault(eleve.getId(), List.of());
                Double coef = coefficientsParMatiere.get(matiereId);

                BulletinMatiereResponse m = new BulletinMatiereResponse();
                m.setMatiereId(matiereId);
                m.setMatiereLibelle(matiereLibelles.get(matiereId));
                m.setCoefficient(coef);
                m.setInterrogation(NoteService.calculerMoyenneInterrogations(notesEleveMatiere));
                m.setDevoir1(NoteService.extraireValeur(notesEleveMatiere, NoteService.DEVOIR, 1));
                m.setDevoir2(NoteService.extraireValeur(notesEleveMatiere, NoteService.DEVOIR, 2));
                m.setMoyenne(moyenne);
                m.setMoyenneCoefficientee(coef != null ? moyenne * coef : null);
                m.setRang(rangParMatiereParEleve.get(matiereId).get(eleve.getId()));
                m.setAppreciation(appreciationPour(moyenne));
                matieres.add(m);
            }
            matieres.sort(Comparator.comparing(BulletinMatiereResponse::getMatiereLibelle,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            r.setMatieres(matieres);

            r.setMoyennePonderee(moyennePondereeParEleve.get(eleve.getId()));
            r.setRangTrimestre(rangTrimestreParEleve.get(eleve.getId()));
            r.setMoyenneAnnuelle(moyenneAnnuelleParEleve.get(eleve.getId()));
            r.setRangAnnuel(rangAnnuelParEleve.get(eleve.getId()));

            appliquerMentions(r, eleve.getId(), periodeId);

            resultats.add(r);
        }
        return resultats;
    }

    private void appliquerMentions(BulletinResponse r, Long eleveId, Long periodeId) {
        Double moyRef = r.getMoyennePonderee();
        boolean suggestionFelicitations = moyRef != null && moyRef >= 16;
        boolean suggestionTableauHonneur = moyRef != null && moyRef >= 12 && moyRef < 16;
        boolean suggestionAvertissement = moyRef != null && moyRef < 10;
        r.setSuggestionFelicitations(suggestionFelicitations);
        r.setSuggestionTableauHonneur(suggestionTableauHonneur);
        r.setSuggestionAvertissement(suggestionAvertissement);

        BulletinMention mention = bulletinMentionRepository.findByEleveIdAndPeriodeId(eleveId, periodeId).orElse(null);
        if (mention != null) {
            r.setTableauHonneur(mention.getTableauHonneur());
            r.setFelicitations(mention.getFelicitations());
            r.setEncouragement(mention.getEncouragement());
            r.setAvertissement(mention.getAvertissement());
            r.setDecisionConseil(mention.getDecisionConseil());
            r.setObservationDirecteur(mention.getObservationDirecteur());
        } else {
            // Rien n'a encore été enregistré : on propose la suggestion à l'écran, sans jamais la
            // persister automatiquement.
            r.setTableauHonneur(suggestionTableauHonneur);
            r.setFelicitations(suggestionFelicitations);
            r.setEncouragement(false);
            r.setAvertissement(suggestionAvertissement);
        }
    }

    @Transactional
    public void enregistrerMentions(Long eleveId, Long periodeId, BulletinMentionRequest form) {
        BulletinMention mention = bulletinMentionRepository.findByEleveIdAndPeriodeId(eleveId, periodeId)
                .orElseGet(BulletinMention::new);
        mention.setEleveId(eleveId);
        mention.setPeriodeId(periodeId);
        mention.setTableauHonneur(form.getTableauHonneur());
        mention.setFelicitations(form.getFelicitations());
        mention.setEncouragement(form.getEncouragement());
        mention.setAvertissement(form.getAvertissement());
        mention.setDecisionConseil(form.getDecisionConseil());
        mention.setObservationDirecteur(form.getObservationDirecteur());
        if (mention.getCode() == null) {
            mention.setCode("BUM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        }
        bulletinMentionRepository.save(mention);
    }

    private Map<Long, Double> calculerMoyennesPonderees(
            List<Long> eleveIds, Map<Long, Map<Long, Double>> moyenneParMatiereParEleve,
            Map<Long, Double> coefficientsParMatiere) {
        Map<Long, Double> resultat = new HashMap<>();
        for (Long eleveId : eleveIds) {
            double sommeCoef = 0;
            double sommePonderee = 0;
            boolean auMoinsUne = false;
            for (Map.Entry<Long, Map<Long, Double>> matiereEntry : moyenneParMatiereParEleve.entrySet()) {
                Double moyenne = matiereEntry.getValue().get(eleveId);
                Double coef = coefficientsParMatiere.get(matiereEntry.getKey());
                if (moyenne != null && coef != null) {
                    sommeCoef += coef;
                    sommePonderee += moyenne * coef;
                    auMoinsUne = true;
                }
            }
            resultat.put(eleveId, auMoinsUne && sommeCoef > 0 ? sommePonderee / sommeCoef : null);
        }
        return resultat;
    }

    /** Moyenne pondérée d'une classe pour une AUTRE période que celle demandée — utilisé
        uniquement pour la moyenne annuelle. */
    private Map<Long, Double> moyennesPartiellesParEleve(
            Long periodeId, List<Long> eleveIds, Map<Long, Double> coefficientsParMatiere) {
        List<Note> notes = noteRepository.findByEleveIdInAndPeriodeId(eleveIds, periodeId);
        Map<Long, Map<Long, List<Note>>> parEleveParMatiere = notes.stream()
                .collect(Collectors.groupingBy(Note::getEleveId, Collectors.groupingBy(Note::getMatiereId)));

        Map<Long, Double> resultat = new HashMap<>();
        for (Long eleveId : eleveIds) {
            Map<Long, List<Note>> parMatiere = parEleveParMatiere.getOrDefault(eleveId, Map.of());
            double sommeCoef = 0;
            double sommePonderee = 0;
            boolean auMoinsUne = false;
            for (Map.Entry<Long, List<Note>> e : parMatiere.entrySet()) {
                Double coef = coefficientsParMatiere.get(e.getKey());
                Double moyenne = extraireMoyenne(e.getValue());
                if (moyenne != null && coef != null) {
                    sommeCoef += coef;
                    sommePonderee += moyenne * coef;
                    auMoinsUne = true;
                }
            }
            resultat.put(eleveId, auMoinsUne && sommeCoef > 0 ? sommePonderee / sommeCoef : null);
        }
        return resultat;
    }

    private Double extraireMoyenne(List<Note> notes) {
        Double moyenneInterrogations = NoteService.calculerMoyenneInterrogations(notes);
        Double devoir1 = NoteService.extraireValeur(notes, NoteService.DEVOIR, 1);
        Double devoir2 = NoteService.extraireValeur(notes, NoteService.DEVOIR, 2);
        return NoteService.calculerMoyenne(moyenneInterrogations, devoir1, devoir2);
    }

    private static String appreciationPour(Double moyenne) {
        if (moyenne == null) return null;
        if (moyenne >= 16) return "Très Bien";
        if (moyenne >= 14) return "Bien";
        if (moyenne >= 12) return "Assez Bien";
        if (moyenne >= 10) return "Moyen";
        if (moyenne >= 8) return "Passable";
        return "Insuffisant";
    }

    /** Classement standard : deux valeurs égales partagent le même rang, le rang suivant saute
        (ex: deux 5e ex-aequo, le suivant est 7e). Les élèves sans valeur n'apparaissent pas. */
    private static Map<Long, Integer> computeRanks(Map<Long, Double> valeurs) {
        List<Map.Entry<Long, Double>> valides = valeurs.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .toList();

        Map<Long, Integer> rangs = new HashMap<>();
        int rang = 0;
        Double precedente = null;
        int position = 0;
        for (Map.Entry<Long, Double> e : valides) {
            position++;
            if (precedente == null || !e.getValue().equals(precedente)) {
                rang = position;
                precedente = e.getValue();
            }
            rangs.put(e.getKey(), rang);
        }
        return rangs;
    }
}
