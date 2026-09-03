package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.response.ProgressionEtapeHistoriqueResponse;
import com.App.lbs_backend.dto.response.ProgressionSaisieNoteResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.Etape;
import com.App.lbs_backend.entity.ProgressionEtapeHistorique;
import com.App.lbs_backend.entity.ProgressionSaisieNote;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.EtapeRepository;
import com.App.lbs_backend.repository.ProfesseurRepository;
import com.App.lbs_backend.repository.ProgressionEtapeHistoriqueRepository;
import com.App.lbs_backend.repository.ProgressionSaisieNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Progression du professeur dans la saisie des notes, colonne par colonne (voir
    ProgressionSaisieNote pour le détail des règles). Les méthodes verrouillerXxx sont réservées
    au professeur assigné (vérifié via classeIds/matiereIds) ; les méthodes deverrouillerXxx sont
    réservées à l'admin côté contrôleur/frontend — aucune vérification professeur ici. */
@Service
@RequiredArgsConstructor
public class ProgressionSaisieNoteService {

    private final ProgressionSaisieNoteRepository progressionRepository;
    private final ProfesseurRepository professeurRepository;
    private final EtapeRepository etapeRepository;
    private final ProgressionEtapeHistoriqueRepository historiqueRepository;
    private final ClasseRepository classeRepository;
    private final com.App.lbs_backend.repository.NoteRepository noteRepository;

    public static final String BROUILLON = "BROUILLON";
    public static final String SOUMISE = "SOUMISE";
    public static final String VALIDEE = "VALIDEE";

    public ProgressionSaisieNoteResponse getProgression(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = progressionRepository
                .findByClasseIdAndMatiereIdAndPeriodeId(classeId, matiereId, periodeId).orElse(null);
        ProgressionSaisieNoteResponse dto = new ProgressionSaisieNoteResponse();
        dto.setClasseId(classeId);
        dto.setMatiereId(matiereId);
        dto.setPeriodeId(periodeId);
        dto.setInterrogationsVerroueesJusqua(p != null && p.getInterrogationsVerroueesJusqua() != null ? p.getInterrogationsVerroueesJusqua() : 0);
        dto.setDevoirsVerrouesJusqua(p != null && p.getDevoirsVerrouesJusqua() != null ? p.getDevoirsVerrouesJusqua() : 0);
        dto.setInterrogationsValideesJusqua(p != null && p.getInterrogationsValideesJusqua() != null ? p.getInterrogationsValideesJusqua() : 0);
        dto.setDevoirsValideesJusqua(p != null && p.getDevoirsValideesJusqua() != null ? p.getDevoirsValideesJusqua() : 0);
        dto.setEtape(codeEtape(p));
        if (p != null) {
            dto.setDateSoumission(p.getDateSoumission());
            dto.setDateValidation(p.getDateValidation());
            dto.setValideParEmail(p.getValideParEmail());
        }
        return dto;
    }

    /** Étape de chaque matière de la classe pour cette période — alimente l'écran de validation
        des bulletins, qui n'autorise l'admin à ouvrir/modifier une matière qu'une fois que le
        professeur l'a soumise (étape SOUMISE ou VALIDEE), jamais tant qu'elle est en BROUILLON. */
    public List<ProgressionSaisieNoteResponse> getProgressionsClasse(Long classeId, Long periodeId) {
        Classe classe = classeRepository.findById(classeId).orElse(null);
        List<Long> matiereIds = classe != null && classe.getMatiereIds() != null ? classe.getMatiereIds() : List.of();
        return matiereIds.stream().map(matiereId -> getProgression(classeId, matiereId, periodeId)).toList();
    }

    /** Auto-soumission déclenchée par {@link ClotureAutomatiqueService} quand la période est déjà
        terminée et que le professeur n'a jamais cliqué "Soumettre" — contrairement à
        {@link #soumettre}, ne vérifie aucune précondition sur les colonnes (le but est justement de
        débloquer une matière restée incomplète) et n'agit que si elle est encore en BROUILLON.
        @return true si une transition a effectivement eu lieu. */
    @Transactional
    public boolean soumettreAutomatiquementSiBrouillon(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        if (!BROUILLON.equals(codeEtape(p))) return false;
        // Fin de période : on fige tout ce qui a été saisi.
        for (Object[] r : noteRepository.findMaxNumeroParType(classeId, matiereId, periodeId)) {
            int max = r[1] != null ? ((Number) r[1]).intValue() : 0;
            if ("INTERROGATION".equals(r[0])) {
                p.setInterrogationsVerroueesJusqua(Math.max(nz(p.getInterrogationsVerroueesJusqua()), max));
            } else if ("DEVOIR".equals(r[0])) {
                p.setDevoirsVerrouesJusqua(Math.max(nz(p.getDevoirsVerrouesJusqua()), max));
            }
        }
        p.setEtapeId(etapeIdPour(SOUMISE));
        p.setDateSoumission(LocalDateTime.now());
        progressionRepository.save(p);
        enregistrerHistorique(p, SOUMISE, "SYSTÈME (auto-soumission — fin de période)");
        return true;
    }

    /** L'enseignant envoie sa matière à l'administration. Les colonnes envoyées sont **figées**
        (grisées de son côté) ; il peut toujours en ajouter de nouvelles ensuite, les remplir puis
        renvoyer (l'envoi couvre alors aussi ces colonnes). Impossible si déjà VALIDÉE.

        @param interrogationsJusqua / devoirsJusqua : envoi sélectif (l'enseignant choisit jusqu'où
               figer). null → on fige toutes les colonnes qui portent une note. Ne peut jamais
               reculer en-dessous de ce qui est déjà figé, ni dépasser ce qui est réellement saisi. */
    @Transactional
    public ProgressionSaisieNoteResponse soumettre(Long classeId, Long matiereId, Long periodeId, Long profId,
                                                   Integer interrogationsJusqua, Integer devoirsJusqua) {
        Professeur professeur = verifierAutorisationProfesseur(profId, classeId, matiereId);
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        if (VALIDEE.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Cette matière est déjà validée par l'administration.");
        }
        // Plus grand numéro de colonne réellement noté, par type.
        int maxInterro = 0, maxDevoir = 0;
        for (Object[] r : noteRepository.findMaxNumeroParType(classeId, matiereId, periodeId)) {
            int max = r[1] != null ? ((Number) r[1]).intValue() : 0;
            if ("INTERROGATION".equals(r[0])) maxInterro = max;
            else if ("DEVOIR".equals(r[0])) maxDevoir = max;
        }
        int cibleInterro = interrogationsJusqua != null ? Math.min(interrogationsJusqua, maxInterro) : maxInterro;
        int cibleDevoir = devoirsJusqua != null ? Math.min(devoirsJusqua, maxDevoir) : maxDevoir;

        int nouvInterro = Math.max(nz(p.getInterrogationsVerroueesJusqua()), cibleInterro);
        int nouvDevoir = Math.max(nz(p.getDevoirsVerrouesJusqua()), cibleDevoir);
        if (nouvInterro == nz(p.getInterrogationsVerroueesJusqua())
                && nouvDevoir == nz(p.getDevoirsVerrouesJusqua())
                && SOUMISE.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Aucune nouvelle colonne à envoyer.");
        }
        p.setInterrogationsVerroueesJusqua(nouvInterro);
        p.setDevoirsVerrouesJusqua(nouvDevoir);
        p.setEtapeId(etapeIdPour(SOUMISE));
        p.setDateSoumission(LocalDateTime.now());
        progressionRepository.save(p);
        enregistrerHistorique(p, SOUMISE, professeur.getEmail());
        return getProgression(classeId, matiereId, periodeId);
    }

    private int nz(Integer v) {
        return v != null ? v : 0;
    }

    /** L'administration valide en bloc toutes les matières d'une classe (déclenché depuis l'écran
        "Validation des bulletins" en même temps que {@code ValidationBulletin}). Chaque matière
        passe à VALIDEE, quel que soit son état de départ (une matière jamais renseignée est
        ignorée : pas de progression, rien à valider). */
    @Transactional
    public void validerToutesMatieresClasse(Long classeId, Long periodeId, String adminEmail) {
        Classe classe = classeRepository.findById(classeId).orElse(null);
        if (classe == null || classe.getMatiereIds() == null) return;
        for (Long matiereId : classe.getMatiereIds()) {
            ProgressionSaisieNote p = progressionRepository
                    .findByClasseIdAndMatiereIdAndPeriodeId(classeId, matiereId, periodeId).orElse(null);
            if (p == null || VALIDEE.equals(codeEtape(p))) continue;
            p.setEtapeId(etapeIdPour(VALIDEE));
            p.setDateValidation(LocalDateTime.now());
            p.setValideParEmail(adminEmail);
            progressionRepository.save(p);
            enregistrerHistorique(p, VALIDEE, adminEmail);
        }
    }

    /** Inverse de {@link #validerToutesMatieresClasse} — repasse les matières VALIDEE à SOUMISE
        (la saisie admin reste possible ; le professeur, lui, ne récupère pas la main). */
    @Transactional
    public void devaliderToutesMatieresClasse(Long classeId, Long periodeId, String adminEmail) {
        Classe classe = classeRepository.findById(classeId).orElse(null);
        if (classe == null || classe.getMatiereIds() == null) return;
        for (Long matiereId : classe.getMatiereIds()) {
            ProgressionSaisieNote p = progressionRepository
                    .findByClasseIdAndMatiereIdAndPeriodeId(classeId, matiereId, periodeId).orElse(null);
            if (p == null || !VALIDEE.equals(codeEtape(p))) continue;
            p.setEtapeId(etapeIdPour(SOUMISE));
            p.setDateValidation(null);
            p.setValideParEmail(null);
            progressionRepository.save(p);
            enregistrerHistorique(p, SOUMISE, adminEmail);
        }
    }

    /** L'admin valide la matière soumise — plus personne ne peut modifier tant qu'elle n'est pas
        dévalidée. */
    @Transactional
    public ProgressionSaisieNoteResponse valider(Long classeId, Long matiereId, Long periodeId, String adminEmail) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        if (!SOUMISE.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Cette matière doit d'abord être soumise par le professeur avant validation.");
        }
        p.setEtapeId(etapeIdPour(VALIDEE));
        p.setDateValidation(LocalDateTime.now());
        p.setValideParEmail(adminEmail);
        progressionRepository.save(p);
        enregistrerHistorique(p, VALIDEE, adminEmail);
        return getProgression(classeId, matiereId, periodeId);
    }

    /** L'admin annule sa validation — repasse à SOUMISE (pas BROUILLON) : le professeur n'a pas
        besoin de resoumettre pour une simple correction admin. */
    @Transactional
    public ProgressionSaisieNoteResponse devaliderMatiere(Long classeId, Long matiereId, Long periodeId, String adminEmail) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        if (!VALIDEE.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Cette matière n'est pas validée.");
        }
        p.setEtapeId(etapeIdPour(SOUMISE));
        p.setDateValidation(null);
        p.setValideParEmail(null);
        progressionRepository.save(p);
        enregistrerHistorique(p, SOUMISE, adminEmail);
        return getProgression(classeId, matiereId, periodeId);
    }

    /** L'administration approuve certaines colonnes reçues (validées jusqu'à interroJusqua /
        devoirsJusqua). Les colonnes approuvées deviennent figées / grisées côté admin aussi.
        Quand toutes les colonnes envoyées sont approuvées, la matière passe VALIDEE.
        null = approuver tout ce qui a été envoyé. Ne peut jamais reculer. */
    @Transactional
    public ProgressionSaisieNoteResponse approuverColonnes(Long classeId, Long matiereId, Long periodeId,
                                                          Integer interroJusqua, Integer devoirsJusqua, String adminEmail) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        if (!SOUMISE.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Cette matière n'est pas en attente d'approbation.");
        }
        int verrInterro = nz(p.getInterrogationsVerroueesJusqua());
        int verrDevoir = nz(p.getDevoirsVerrouesJusqua());
        int cibleInterro = interroJusqua != null ? Math.min(interroJusqua, verrInterro) : verrInterro;
        int cibleDevoir = devoirsJusqua != null ? Math.min(devoirsJusqua, verrDevoir) : verrDevoir;

        int nouvInterro = Math.max(nz(p.getInterrogationsValideesJusqua()), cibleInterro);
        int nouvDevoir = Math.max(nz(p.getDevoirsValideesJusqua()), cibleDevoir);
        if (nouvInterro == nz(p.getInterrogationsValideesJusqua())
                && nouvDevoir == nz(p.getDevoirsValideesJusqua())) {
            throw new IllegalArgumentException("Aucune nouvelle colonne à approuver.");
        }
        p.setInterrogationsValideesJusqua(nouvInterro);
        p.setDevoirsValideesJusqua(nouvDevoir);

        boolean toutApprouve = nouvInterro >= verrInterro && nouvDevoir >= verrDevoir;
        if (toutApprouve) {
            p.setEtapeId(etapeIdPour(VALIDEE));
            p.setDateValidation(LocalDateTime.now());
            p.setValideParEmail(adminEmail);
            progressionRepository.save(p);
            enregistrerHistorique(p, VALIDEE, adminEmail);
        } else {
            progressionRepository.save(p);
        }
        return getProgression(classeId, matiereId, periodeId);
    }

    /** L'administration renvoie une matière (reçue OU approuvée) à l'enseignant → BROUILLON : il
        récupère la main pour corriger / ajouter une interrogation, puis la renvoie. */
    @Transactional
    public ProgressionSaisieNoteResponse renvoyerAuProfesseur(Long classeId, Long matiereId, Long periodeId, String adminEmail) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        remettreEnBrouillon(p, adminEmail != null ? adminEmail + " (renvoi à l'enseignant)" : "ADMIN (renvoi)");
        return getProgression(classeId, matiereId, periodeId);
    }

    /** L'enseignant reprend lui-même une matière qu'il a envoyée trop tôt (SOUMISE → BROUILLON) —
        typiquement pour ajouter une interrogation supplémentaire — tant que l'administration ne l'a
        pas encore validée. */
    @Transactional
    public ProgressionSaisieNoteResponse reprendre(Long classeId, Long matiereId, Long periodeId, Long profId) {
        Professeur professeur = verifierAutorisationProfesseur(profId, classeId, matiereId);
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        remettreEnBrouillon(p, professeur.getEmail() + " (reprise de la saisie)");
        return getProgression(classeId, matiereId, periodeId);
    }

    /** Repasse silencieusement une matière SOUMISE en BROUILLON — appelé quand l'enseignant
        remodifie/ajoute une interrogation après avoir envoyé (tant que ce n'est pas validé).
        No-op si la matière n'est pas au stade SOUMISE. */
    @Transactional
    public void reprendreSiEnvoyee(Long classeId, Long matiereId, Long periodeId, String auteur) {
        ProgressionSaisieNote p = progressionRepository
                .findByClasseIdAndMatiereIdAndPeriodeId(classeId, matiereId, periodeId).orElse(null);
        if (p == null || !SOUMISE.equals(codeEtape(p))) return;
        p.setEtapeId(etapeIdPour(BROUILLON));
        p.setDateSoumission(null);
        progressionRepository.save(p);
        enregistrerHistorique(p, BROUILLON, auteur);
    }

    private void remettreEnBrouillon(ProgressionSaisieNote p, String auteur) {
        if (BROUILLON.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Cette matière n'a pas été envoyée.");
        }
        p.setEtapeId(etapeIdPour(BROUILLON));
        p.setDateSoumission(null);
        p.setDateValidation(null);
        p.setValideParEmail(null);
        p.setInterrogationsVerroueesJusqua(0);
        p.setDevoirsVerrouesJusqua(0);
        p.setInterrogationsValideesJusqua(0);
        p.setDevoirsValideesJusqua(0);
        progressionRepository.save(p);
        enregistrerHistorique(p, BROUILLON, auteur);
    }

    /** Liste chronologique des transitions d'étape de cette matière — audit complet, jamais purgé. */
    public List<ProgressionEtapeHistoriqueResponse> getHistorique(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = progressionRepository
                .findByClasseIdAndMatiereIdAndPeriodeId(classeId, matiereId, periodeId).orElse(null);
        if (p == null) return List.of();
        return historiqueRepository.findByProgressionIdOrderByDateTransitionAsc(p.getId()).stream()
                .map(h -> {
                    ProgressionEtapeHistoriqueResponse dto = new ProgressionEtapeHistoriqueResponse();
                    String code = etapeRepository.findById(h.getEtapeId()).map(Etape::getCode).orElse(null);
                    dto.setEtape(code);
                    dto.setEtapeLibelle(etapeRepository.findById(h.getEtapeId()).map(Etape::getLibelle).orElse(code));
                    dto.setDateTransition(h.getDateTransition());
                    dto.setAuteurEmail(h.getAuteurEmail());
                    return dto;
                })
                .toList();
    }

    private void enregistrerHistorique(ProgressionSaisieNote p, String etapeCode, String auteurEmail) {
        ProgressionEtapeHistorique h = new ProgressionEtapeHistorique();
        h.setProgressionId(p.getId());
        h.setEtapeId(etapeIdPour(etapeCode));
        h.setAuteurEmail(auteurEmail);
        historiqueRepository.save(h);
    }

    private String codeEtape(ProgressionSaisieNote p) {
        if (p == null || p.getEtapeId() == null) return BROUILLON;
        return etapeRepository.findById(p.getEtapeId()).map(Etape::getCode).orElse(BROUILLON);
    }

    private Long etapeIdPour(String code) {
        return etapeRepository.findByCode(code)
                .map(Etape::getId)
                .orElseThrow(() -> new IllegalStateException(
                        "Étape '" + code + "' introuvable dans le référentiel — vérifier le seed de lbs_etape."));
    }

    @Transactional
    public ProgressionSaisieNoteResponse verrouillerInterrogation(Long classeId, Long matiereId, Long periodeId, int numero, Long profId) {
        verifierAutorisationProfesseur(profId, classeId, matiereId);
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int actuel = p.getInterrogationsVerroueesJusqua() != null ? p.getInterrogationsVerroueesJusqua() : 0;
        if (numero != actuel + 1) {
            throw new IllegalArgumentException(
                    "Vous devez verrouiller les interrogations dans l'ordre (la prochaine à verrouiller est l'interrogation " + (actuel + 1) + ").");
        }
        if (numero > 1) {
            int validees = p.getInterrogationsValideesJusqua() != null ? p.getInterrogationsValideesJusqua() : 0;
            if (validees < numero - 1) {
                throw new IllegalArgumentException(
                        "L'interrogation " + (numero - 1) + " doit d'abord être validée par l'administration avant de continuer.");
            }
        }
        p.setInterrogationsVerroueesJusqua(numero);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    @Transactional
    public ProgressionSaisieNoteResponse verrouillerDevoir(Long classeId, Long matiereId, Long periodeId, int numero, Long profId) {
        verifierAutorisationProfesseur(profId, classeId, matiereId);
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int actuel = p.getDevoirsVerrouesJusqua() != null ? p.getDevoirsVerrouesJusqua() : 0;
        if (numero != actuel + 1) {
            throw new IllegalArgumentException(
                    "Vous devez verrouiller les devoirs dans l'ordre (le prochain à verrouiller est le devoir " + (actuel + 1) + ").");
        }
        if (numero > 1) {
            int validees = p.getDevoirsValideesJusqua() != null ? p.getDevoirsValideesJusqua() : 0;
            if (validees < numero - 1) {
                throw new IllegalArgumentException(
                        "Le devoir " + (numero - 1) + " doit d'abord être validé par l'administration avant de continuer.");
            }
        }
        p.setDevoirsVerrouesJusqua(numero);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    @Transactional
    public ProgressionSaisieNoteResponse deverrouillerInterrogation(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int actuel = p.getInterrogationsVerroueesJusqua() != null ? p.getInterrogationsVerroueesJusqua() : 0;
        int nouveau = Math.max(0, actuel - 1);
        p.setInterrogationsVerroueesJusqua(nouveau);
        int validees = p.getInterrogationsValideesJusqua() != null ? p.getInterrogationsValideesJusqua() : 0;
        if (validees > nouveau) p.setInterrogationsValideesJusqua(nouveau);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    @Transactional
    public ProgressionSaisieNoteResponse deverrouillerDevoir(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int actuel = p.getDevoirsVerrouesJusqua() != null ? p.getDevoirsVerrouesJusqua() : 0;
        int nouveau = Math.max(0, actuel - 1);
        p.setDevoirsVerrouesJusqua(nouveau);
        int validees = p.getDevoirsValideesJusqua() != null ? p.getDevoirsValideesJusqua() : 0;
        if (validees > nouveau) p.setDevoirsValideesJusqua(nouveau);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    /** L'admin valide une colonne d'interrogation précise — obligatoire dans l'ordre, et seulement
        si elle a déjà été verrouillée par le professeur. Débloque la colonne suivante pour lui. */
    @Transactional
    public ProgressionSaisieNoteResponse validerInterrogation(Long classeId, Long matiereId, Long periodeId, int numero) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int verrouees = p.getInterrogationsVerroueesJusqua() != null ? p.getInterrogationsVerroueesJusqua() : 0;
        int validees = p.getInterrogationsValideesJusqua() != null ? p.getInterrogationsValideesJusqua() : 0;
        if (numero > verrouees) {
            throw new IllegalArgumentException("Cette interrogation n'a pas encore été verrouillée par le professeur.");
        }
        if (numero != validees + 1) {
            throw new IllegalArgumentException(
                    "Vous devez valider les interrogations dans l'ordre (la prochaine à valider est l'interrogation " + (validees + 1) + ").");
        }
        p.setInterrogationsValideesJusqua(numero);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    @Transactional
    public ProgressionSaisieNoteResponse validerDevoir(Long classeId, Long matiereId, Long periodeId, int numero) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int verrouees = p.getDevoirsVerrouesJusqua() != null ? p.getDevoirsVerrouesJusqua() : 0;
        int validees = p.getDevoirsValideesJusqua() != null ? p.getDevoirsValideesJusqua() : 0;
        if (numero > verrouees) {
            throw new IllegalArgumentException("Ce devoir n'a pas encore été verrouillé par le professeur.");
        }
        if (numero != validees + 1) {
            throw new IllegalArgumentException(
                    "Vous devez valider les devoirs dans l'ordre (le prochain à valider est le devoir " + (validees + 1) + ").");
        }
        p.setDevoirsValideesJusqua(numero);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    private ProgressionSaisieNote obtenirOuCreer(Long classeId, Long matiereId, Long periodeId) {
        return progressionRepository.findByClasseIdAndMatiereIdAndPeriodeId(classeId, matiereId, periodeId)
                .orElseGet(() -> {
                    ProgressionSaisieNote p = new ProgressionSaisieNote();
                    p.setClasseId(classeId);
                    p.setMatiereId(matiereId);
                    p.setPeriodeId(periodeId);
                    p.setCode("PRG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
                    p.setEtapeId(etapeIdPour(BROUILLON));
                    return p;
                });
    }

    private Professeur verifierAutorisationProfesseur(Long profId, Long classeId, Long matiereId) {
        Professeur professeur = professeurRepository.findById(profId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));
        boolean autorise = professeur.getClasseIds() != null && professeur.getClasseIds().contains(classeId)
                && professeur.getMatiereIds() != null && professeur.getMatiereIds().contains(matiereId);
        if (!autorise) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier cette classe/matière.");
        }
        return professeur;
    }
}
