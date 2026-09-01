package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.BulletinMentionRequest;
import com.App.lbs_backend.dto.response.BulletinMatiereResponse;
import com.App.lbs_backend.dto.response.BulletinResponse;
import com.App.lbs_backend.dto.response.MoyennePeriodeResponse;
import com.App.lbs_backend.core.utils.ReportService;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private final ReportService reportService;

    /** Génère le PDF d'un bulletin (un seul élève) via JasperReports. */
    public byte[] genererBulletinPdfBytes(Long eleveId, Long periodeId) throws Exception {
        BulletinResponse bulletin = genererBulletin(eleveId, periodeId);
        return reportService.generatePdfReport("bulletin", construirePdfParams(bulletin), bulletin.getMatieres());
    }

    /** Construit les paramètres Jasper (en-tête, résumé, logo, filigrane) pour un bulletin déjà
        calculé — utilisé aussi bien pour un PDF unique que pour fusionner les bulletins d'une
        classe entière (un {@link net.sf.jasperreports.engine.JasperPrint} par élève). */
    public Map<String, Object> construirePdfParams(BulletinResponse b) throws java.io.IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("logoImage", new ClassPathResource("images/logo.png").getInputStream());
        params.put("watermarkImage", new ClassPathResource("images/watermark.png").getInputStream());
        params.put("eleveNomComplet", b.getEleveNomComplet());
        params.put("eleveMatricule", b.getEleveMatricule() != null ? b.getEleveMatricule() : "—");
        params.put("classeLibelle", b.getClasseLibelle());
        params.put("effectifClasse", String.valueOf(b.getEffectifClasse()));
        params.put("periodeLibelle", b.getPeriodeLibelle());
        params.put("anneeScolaireLibelle", b.getAnneeScolaireLibelle());
        params.put("moyennePonderee", formatMoyenne(b.getMoyennePonderee()));
        params.put("rangTrimestre", formatRang(b.getRangTrimestre()));
        params.put("moyenneAnnuelle", formatMoyenne(b.getMoyenneAnnuelle()));
        params.put("rangAnnuel", formatRang(b.getRangAnnuel()));
        params.put("tableauHonneur", formatOuiNon(b.getTableauHonneur()));
        params.put("felicitations", formatOuiNon(b.getFelicitations()));
        params.put("encouragement", formatOuiNon(b.getEncouragement()));
        params.put("avertissement", formatOuiNon(b.getAvertissement()));
        params.put("decisionConseil", b.getDecisionConseil() != null ? b.getDecisionConseil() : "—");
        params.put("observationDirecteur", b.getObservationDirecteur() != null ? b.getObservationDirecteur() : "—");

        List<MoyennePeriodeResponse> periodes = b.getMoyennesParPeriode() != null ? b.getMoyennesParPeriode() : List.of();
        for (int i = 0; i < 3; i++) {
            String suffixe = String.valueOf(i + 1);
            if (i < periodes.size()) {
                params.put("trimestre" + suffixe + "Libelle", periodes.get(i).getPeriodeLibelle());
                params.put("trimestre" + suffixe + "Moyenne", formatMoyenne(periodes.get(i).getMoyenne()));
            } else {
                params.put("trimestre" + suffixe + "Libelle", "—");
                params.put("trimestre" + suffixe + "Moyenne", "—");
            }
        }
        return params;
    }

    private static String formatMoyenne(Double valeur) {
        return valeur != null ? String.format(Locale.FRANCE, "%.2f", valeur) : "—";
    }

    private static String formatRang(Integer rang) {
        return rang != null ? rang + "e" : "—";
    }

    private static String formatOuiNon(Boolean valeur) {
        return Boolean.TRUE.equals(valeur) ? "Oui" : "Non";
    }

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

        List<Note> notes = noteRepository.findByEleveIdInAndPeriodeIdAndClasseId(eleveIds, periodeId, classeId);

        Map<Long, Map<Long, List<Note>>> parMatiereParEleve = notes.stream()
                .collect(Collectors.groupingBy(Note::getMatiereId, Collectors.groupingBy(Note::getEleveId)));

        // Par matière : nombre d'interros / de devoirs réellement organisés pour la classe
        // (dénominateur des moyennes ; une évaluation non composée par l'élève compte 0).
        Map<Long, List<Note>> notesParMatiere = notes.stream().collect(Collectors.groupingBy(Note::getMatiereId));
        Map<Long, Integer> nbInterrosParMatiere = new HashMap<>();
        Map<Long, Integer> nbDevoirsParMatiere = new HashMap<>();
        notesParMatiere.forEach((matiereId, notesMatiere) -> {
            nbInterrosParMatiere.put(matiereId, NoteService.nombreEvaluationsOrganisees(
                    notesMatiere, NoteService.INTERROGATION, NoteService.MAX_INTERROGATIONS));
            nbDevoirsParMatiere.put(matiereId, NoteService.nombreEvaluationsOrganisees(
                    notesMatiere, NoteService.DEVOIR, NoteService.MAX_DEVOIRS));
        });

        // Le bulletin doit lister TOUTES les matières attribuées à cette classe (référentiel
        // Classes), pas seulement celles qui ont déjà au moins une note quelque part — une matière
        // jamais évaluée reste affichée, avec 0 et la mention "N'a pas composé". La liste de la
        // classe est la SEULE source de vérité dès qu'elle est renseignée : si une matière en est
        // retirée, elle disparaît du bulletin même si d'anciennes notes existent encore pour elle
        // (l'historique reste consultable en rouvrant l'écran de saisie pour cette matière).
        // Ce n'est que si la classe n'a encore AUCUNE matière explicitement assignée (ancienne
        // donnée) qu'on retombe sur les matières ayant un coefficient pour son niveau, complétées
        // par toute matière ayant déjà des notes, pour ne pas produire un bulletin vide.
        Set<Long> toutesMatieresId;
        if (classe.getMatiereIds() != null && !classe.getMatiereIds().isEmpty()) {
            toutesMatieresId = new HashSet<>(classe.getMatiereIds());
        } else {
            toutesMatieresId = new HashSet<>(coefficientsParMatiere.keySet());
            toutesMatieresId.addAll(parMatiereParEleve.keySet());
        }

        Map<Long, String> matiereLibelles = new HashMap<>();
        Map<Long, Boolean> estConduiteParMatiere = new HashMap<>();
        for (Long matiereId : toutesMatieresId) {
            matiereRepository.findById(matiereId).ifPresent(m -> {
                matiereLibelles.put(matiereId, m.getLibelle());
                estConduiteParMatiere.put(matiereId, Boolean.TRUE.equals(m.getEstConduite()));
            });
        }

        Map<Long, Map<Long, Double>> moyenneParMatiereParEleve = new HashMap<>();
        for (Long matiereId : toutesMatieresId) {
            boolean estConduite = Boolean.TRUE.equals(estConduiteParMatiere.get(matiereId));
            int nbInterros = estConduite ? 1 : nbInterrosParMatiere.getOrDefault(matiereId, 0);
            int nbDevoirs = estConduite ? 0 : nbDevoirsParMatiere.getOrDefault(matiereId, 0);
            Map<Long, List<Note>> parEleveNotes = parMatiereParEleve.getOrDefault(matiereId, Map.of());
            Map<Long, Double> parEleve = new HashMap<>();
            for (Long eleveId : eleveIds) {
                List<Note> notesEleve = parEleveNotes.getOrDefault(eleveId, List.of());
                parEleve.put(eleveId, extraireMoyenne(notesEleve, nbInterros, nbDevoirs, estConduite));
            }
            moyenneParMatiereParEleve.put(matiereId, parEleve);
        }

        Map<Long, Map<Long, Integer>> rangParMatiereParEleve = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Double>> matiereEntry : moyenneParMatiereParEleve.entrySet()) {
            rangParMatiereParEleve.put(matiereEntry.getKey(), computeRanks(matiereEntry.getValue()));
        }

        Map<Long, Double> moyennePondereeParEleve = calculerMoyennesPonderees(
                eleveIds, moyenneParMatiereParEleve, coefficientsParMatiere);
        Map<Long, Integer> rangTrimestreParEleve = computeRanks(moyennePondereeParEleve);

        List<PeriodeAcademique> periodesAnnee = periode.getAnneeScolaireId() == null
                ? List.of()
                : periodeAcademiqueRepository.findByAnneeScolaireIdOrderByDateDebutAsc(periode.getAnneeScolaireId());

        Map<Long, List<Double>> moyennesAnnuelles = new HashMap<>();
        // Conserve la moyenne pondérée de CHAQUE période de l'année (pas seulement celle demandée)
        // pour alimenter le récapitulatif "Moyenne du 1er/2e/3e Trimestre" du bulletin.
        Map<Long, Map<Long, Double>> moyenneParPeriodeParEleve = new HashMap<>();
        for (PeriodeAcademique p : periodesAnnee) {
            Map<Long, Double> moyennesPeriode = p.getId().equals(periodeId)
                    ? moyennePondereeParEleve
                    : moyennesPartiellesParEleve(p.getId(), eleveIds, coefficientsParMatiere);
            moyenneParPeriodeParEleve.put(p.getId(), moyennesPeriode);
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
            r.setEleveMatricule(eleve.getCode());
            r.setClasseId(classeId);
            r.setClasseLibelle(classe.getLibelle());
            r.setEffectifClasse(eleves.size());
            r.setPeriodeId(periodeId);
            r.setPeriodeLibelle(periode.getLibelle());
            if (periode.getAnneeScolaire() != null) r.setAnneeScolaireLibelle(periode.getAnneeScolaire().getLibelle());

            List<BulletinMatiereResponse> matieres = new ArrayList<>();
            for (Long matiereId : toutesMatieresId) {
                Double moyenne = moyenneParMatiereParEleve.getOrDefault(matiereId, Map.of()).get(eleve.getId());

                List<Note> notesEleveMatiere = parMatiereParEleve.getOrDefault(matiereId, Map.of())
                        .getOrDefault(eleve.getId(), List.of());
                boolean nonCompose = notesEleveMatiere.isEmpty();
                boolean estConduiteMat = Boolean.TRUE.equals(estConduiteParMatiere.get(matiereId));
                Double coef = coefficientsParMatiere.get(matiereId);
                int nbInterros = estConduiteMat ? 1 : nbInterrosParMatiere.getOrDefault(matiereId, 0);

                BulletinMatiereResponse m = new BulletinMatiereResponse();
                m.setMatiereId(matiereId);
                m.setMatiereLibelle(matiereLibelles.get(matiereId));
                m.setCoefficient(coef);
                m.setInterrogation(NoteService.calculerMoyenneInterrogations(notesEleveMatiere, nbInterros));
                m.setDevoir1(NoteService.extraireValeur(notesEleveMatiere, NoteService.DEVOIR, 1));
                m.setDevoir2(NoteService.extraireValeur(notesEleveMatiere, NoteService.DEVOIR, 2));
                m.setMoyenne(moyenne);
                m.setMoyenneCoefficientee(coef != null && moyenne != null ? moyenne * coef : null);
                m.setRang(rangParMatiereParEleve.getOrDefault(matiereId, Map.of()).get(eleve.getId()));
                m.setNonCompose(nonCompose);
                m.setAppreciation(nonCompose ? "N'a pas composé" : appreciationPour(moyenne));
                matieres.add(m);
            }
            // CONDUITE est notée à part par l'administrateur (pas de professeur assigné) — on la
            // fait toujours apparaître en dernière ligne du tableau, comme sur le bulletin papier
            // de référence, plutôt qu'au tri alphabétique.
            matieres.sort(Comparator
                    .comparing((BulletinMatiereResponse m) -> Boolean.TRUE.equals(estConduiteParMatiere.get(m.getMatiereId())))
                    .thenComparing(BulletinMatiereResponse::getMatiereLibelle, Comparator.nullsLast(Comparator.naturalOrder())));
            r.setMatieres(matieres);

            r.setMoyennePonderee(moyennePondereeParEleve.get(eleve.getId()));
            r.setRangTrimestre(rangTrimestreParEleve.get(eleve.getId()));
            r.setMoyenneAnnuelle(moyenneAnnuelleParEleve.get(eleve.getId()));
            r.setRangAnnuel(rangAnnuelParEleve.get(eleve.getId()));

            List<MoyennePeriodeResponse> moyennesParPeriode = new ArrayList<>();
            for (PeriodeAcademique p : periodesAnnee) {
                MoyennePeriodeResponse mp = new MoyennePeriodeResponse();
                mp.setPeriodeLibelle(p.getLibelle());
                mp.setMoyenne(moyenneParPeriodeParEleve.getOrDefault(p.getId(), Map.of()).get(eleve.getId()));
                moyennesParPeriode.add(mp);
            }
            r.setMoyennesParPeriode(moyennesParPeriode);

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

        // Dénominateurs des moyennes, par matière, à l'échelle de la classe.
        Map<Long, List<Note>> notesParMatiere = notes.stream().collect(Collectors.groupingBy(Note::getMatiereId));
        Map<Long, Integer> nbInterrosParMatiere = new HashMap<>();
        Map<Long, Integer> nbDevoirsParMatiere = new HashMap<>();
        notesParMatiere.forEach((matiereId, notesMatiere) -> {
            nbInterrosParMatiere.put(matiereId, NoteService.nombreEvaluationsOrganisees(
                    notesMatiere, NoteService.INTERROGATION, NoteService.MAX_INTERROGATIONS));
            nbDevoirsParMatiere.put(matiereId, NoteService.nombreEvaluationsOrganisees(
                    notesMatiere, NoteService.DEVOIR, NoteService.MAX_DEVOIRS));
        });

        Map<Long, Double> resultat = new HashMap<>();
        for (Long eleveId : eleveIds) {
            Map<Long, List<Note>> parMatiere = parEleveParMatiere.getOrDefault(eleveId, Map.of());
            double sommeCoef = 0;
            double sommePonderee = 0;
            boolean auMoinsUne = false;
            for (Map.Entry<Long, List<Note>> e : parMatiere.entrySet()) {
                Double coef = coefficientsParMatiere.get(e.getKey());
                boolean estConduite = matiereRepository.findById(e.getKey())
                        .map(m -> Boolean.TRUE.equals(m.getEstConduite())).orElse(false);
                int nbInterros = estConduite ? 1 : nbInterrosParMatiere.getOrDefault(e.getKey(), 0);
                int nbDevoirs = estConduite ? 0 : nbDevoirsParMatiere.getOrDefault(e.getKey(), 0);
                Double moyenne = extraireMoyenne(e.getValue(), nbInterros, nbDevoirs, estConduite);
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

    private Double extraireMoyenne(List<Note> notes, int nbInterros, int nbDevoirs, boolean estConduite) {
        Double moyenneInterrogations = NoteService.calculerMoyenneInterrogations(notes, nbInterros);
        Double moyenneDevoirs = estConduite ? null
                : NoteService.moyenneComposante(notes, NoteService.DEVOIR, nbDevoirs);
        return NoteService.calculerMoyenneMatiere(moyenneInterrogations, moyenneDevoirs, estConduite);
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
