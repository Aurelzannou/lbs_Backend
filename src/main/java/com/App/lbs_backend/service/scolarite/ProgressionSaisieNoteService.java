package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.response.ProgressionSaisieNoteResponse;
import com.App.lbs_backend.entity.Etape;
import com.App.lbs_backend.entity.ProgressionSaisieNote;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.repository.EtapeRepository;
import com.App.lbs_backend.repository.ProfesseurRepository;
import com.App.lbs_backend.repository.ProgressionSaisieNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        dto.setEtape(codeEtape(p));
        if (p != null) {
            dto.setDateSoumission(p.getDateSoumission());
            dto.setDateValidation(p.getDateValidation());
            dto.setValideParEmail(p.getValideParEmail());
        }
        return dto;
    }

    /** Le professeur soumet la matière entière pour validation admin — nécessite que toutes les
        colonnes soient déjà verrouillées (au moins 1 interrogation, les 2 devoirs). */
    @Transactional
    public ProgressionSaisieNoteResponse soumettre(Long classeId, Long matiereId, Long periodeId, Long profId) {
        verifierAutorisationProfesseur(profId, classeId, matiereId);
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        if (!BROUILLON.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Cette matière a déjà été soumise pour validation.");
        }
        int interroVerrouees = p.getInterrogationsVerroueesJusqua() != null ? p.getInterrogationsVerroueesJusqua() : 0;
        int devoirsVerroues = p.getDevoirsVerrouesJusqua() != null ? p.getDevoirsVerrouesJusqua() : 0;
        if (interroVerrouees < 1 || devoirsVerroues < 2) {
            throw new IllegalArgumentException(
                    "Vous devez d'abord terminer toutes les colonnes (interrogations et devoirs) avant de soumettre.");
        }
        p.setEtapeId(etapeIdPour(SOUMISE));
        p.setDateSoumission(LocalDateTime.now());
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
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
        return getProgression(classeId, matiereId, periodeId);
    }

    /** L'admin annule sa validation — repasse à SOUMISE (pas BROUILLON) : le professeur n'a pas
        besoin de resoumettre pour une simple correction admin. */
    @Transactional
    public ProgressionSaisieNoteResponse devaliderMatiere(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        if (!VALIDEE.equals(codeEtape(p))) {
            throw new IllegalArgumentException("Cette matière n'est pas validée.");
        }
        p.setEtapeId(etapeIdPour(SOUMISE));
        p.setDateValidation(null);
        p.setValideParEmail(null);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
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
        p.setDevoirsVerrouesJusqua(numero);
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    @Transactional
    public ProgressionSaisieNoteResponse deverrouillerInterrogation(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int actuel = p.getInterrogationsVerroueesJusqua() != null ? p.getInterrogationsVerroueesJusqua() : 0;
        p.setInterrogationsVerroueesJusqua(Math.max(0, actuel - 1));
        progressionRepository.save(p);
        return getProgression(classeId, matiereId, periodeId);
    }

    @Transactional
    public ProgressionSaisieNoteResponse deverrouillerDevoir(Long classeId, Long matiereId, Long periodeId) {
        ProgressionSaisieNote p = obtenirOuCreer(classeId, matiereId, periodeId);
        int actuel = p.getDevoirsVerrouesJusqua() != null ? p.getDevoirsVerrouesJusqua() : 0;
        p.setDevoirsVerrouesJusqua(Math.max(0, actuel - 1));
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

    private void verifierAutorisationProfesseur(Long profId, Long classeId, Long matiereId) {
        Professeur professeur = professeurRepository.findById(profId)
                .orElseThrow(() -> new IllegalArgumentException("Professeur introuvable"));
        boolean autorise = professeur.getClasseIds() != null && professeur.getClasseIds().contains(classeId)
                && professeur.getMatiereIds() != null && professeur.getMatiereIds().contains(matiereId);
        if (!autorise) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier cette classe/matière.");
        }
    }
}
