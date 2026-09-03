package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.FeuilleSaisieNotesRequest;
import com.App.lbs_backend.dto.response.FeuilleSaisieNotesResponse;
import com.App.lbs_backend.dto.response.ValidationBulletinResponse;
import com.App.lbs_backend.entity.Acte;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.PeriodeAcademique;
import com.App.lbs_backend.entity.TypeActe;
import com.App.lbs_backend.entity.ValidationBulletin;
import com.App.lbs_backend.repository.ActeRepository;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.PeriodeAcademiqueRepository;
import com.App.lbs_backend.repository.TypeActeRepository;
import com.App.lbs_backend.repository.ValidationBulletinRepository;
import com.App.lbs_backend.service.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationBulletinService {

    private static final String CODE_TYPE_ACTE_BULLETIN = "BULLETIN";

    private final ValidationBulletinRepository validationBulletinRepository;
    private final ClasseRepository classeRepository;
    private final PeriodeAcademiqueRepository periodeAcademiqueRepository;
    private final EleveRepository eleveRepository;
    private final ActeRepository acteRepository;
    private final TypeActeRepository typeActeRepository;
    private final BulletinService bulletinService;
    private final MinioStorageService minioStorageService;
    private final NoteService noteService;
    private final ProgressionSaisieNoteService progressionSaisieNoteService;

    /** Service de correction des notes propre à cet écran — voir
        {@link NoteService#enregistrerFeuilleCorrection}. Aucune restriction de période/année :
        l'admin peut corriger les notes de n'importe quelle période depuis cet écran, contrairement
        au service de saisie (portail professeur + écran "Saisie des notes") qui, lui, reste limité
        à la période en cours. */
    public FeuilleSaisieNotesResponse corrigerNotes(FeuilleSaisieNotesRequest form) {
        return noteService.enregistrerFeuilleCorrection(form);
    }

    /** Statut de validation de chaque classe pour une période — alimente l'écran de validation. */
    public List<ValidationBulletinResponse> listerParPeriode(Long periodeId, Long anneeScolaireId) {
        Map<Long, ValidationBulletin> parClasse = validationBulletinRepository
                .findByPeriodeIdAndAnneeScolaireId(periodeId, anneeScolaireId).stream()
                .collect(Collectors.toMap(ValidationBulletin::getClasseId, v -> v));

        List<Classe> classes = classeRepository.findAll();

        return classes.stream().map(classe -> {
            ValidationBulletinResponse dto = new ValidationBulletinResponse();
            dto.setClasseId(classe.getId());
            dto.setClasseLibelle(classe.getLibelle());
            dto.setPeriodeId(periodeId);
            dto.setAnneeScolaireId(anneeScolaireId);

            ValidationBulletin existant = parClasse.get(classe.getId());
            if (existant != null) {
                dto.setValide(Boolean.TRUE.equals(existant.getValide()));
                dto.setDateValidation(existant.getDateValidation());
                dto.setValideParEmail(existant.getValideParEmail());
            }

            List<Long> matiereIds = classe.getMatiereIds() != null ? classe.getMatiereIds() : List.of();
            dto.setMatieresTotal(matiereIds.size());
            // "reçues" = approuvées par l'administration (étape VALIDEE) — c'est le prérequis pour
            // valider le bulletin de la classe.
            long approuvees = progressionSaisieNoteService.getProgressionsClasse(classe.getId(), periodeId).stream()
                    .filter(p -> "VALIDEE".equals(p.getEtape()))
                    .count();
            dto.setMatieresRecues((int) approuvees);
            return dto;
        }).collect(Collectors.toList());
    }

    public ValidationBulletinResponse getStatut(Long classeId, Long periodeId) {
        ValidationBulletin existant = validationBulletinRepository
                .findByClasseIdAndPeriodeId(classeId, periodeId).orElse(null);

        ValidationBulletinResponse dto = new ValidationBulletinResponse();
        dto.setClasseId(classeId);
        dto.setPeriodeId(periodeId);
        if (existant != null) {
            dto.setAnneeScolaireId(existant.getAnneeScolaireId());
            dto.setValide(Boolean.TRUE.equals(existant.getValide()));
            dto.setDateValidation(existant.getDateValidation());
            dto.setValideParEmail(existant.getValideParEmail());
        }
        return dto;
    }

    public ValidationBulletinResponse valider(Long classeId, Long periodeId, Long anneeScolaireId, String email) {
        ValidationBulletin vb = validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId)
                .orElseGet(ValidationBulletin::new);
        vb.setClasseId(classeId);
        vb.setPeriodeId(periodeId);
        vb.setAnneeScolaireId(anneeScolaireId);
        vb.setValide(true);
        vb.setDateValidation(LocalDateTime.now());
        vb.setValideParEmail(email);
        if (vb.getCode() == null) {
            vb.setCode("VAB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        }
        validationBulletinRepository.save(vb);
        // Valider la classe = valider toutes ses matières (cohérence tableau de bord / écran notes).
        progressionSaisieNoteService.validerToutesMatieresClasse(classeId, periodeId, email);
        archiverBulletinsClasse(classeId, periodeId);
        return getStatut(classeId, periodeId);
    }

    /** Génère et archive (MinIO + entité Acte) le bulletin PDF de chaque élève de la classe au
        moment de la validation — garde une trace immuable de ce qui a réellement été remis aux
        parents, même si les notes sont modifiées plus tard suite à une dévalidation. Chaque
        validation crée un nouvel acte (historique append-only) ; un échec ponctuel (MinIO
        indisponible, etc.) est journalisé sans bloquer la validation elle-même. */
    private void archiverBulletinsClasse(Long classeId, Long periodeId) {
        Classe classe = classeRepository.findById(classeId).orElse(null);
        PeriodeAcademique periode = periodeAcademiqueRepository.findById(periodeId).orElse(null);
        if (classe == null || periode == null) return;

        TypeActe typeBulletin = typeActeRepository.findByCode(CODE_TYPE_ACTE_BULLETIN)
                .orElseGet(() -> {
                    TypeActe t = new TypeActe();
                    t.setCode(CODE_TYPE_ACTE_BULLETIN);
                    t.setLibelle("Bulletin de notes");
                    return typeActeRepository.save(t);
                });

        List<Eleve> eleves = eleveRepository.findByClasseIdOrderByNomAscPrenomAsc(classeId);
        for (Eleve eleve : eleves) {
            try {
                byte[] pdf = bulletinService.genererBulletinPdfBytes(eleve.getId(), periodeId);
                String nomFichier = "bulletin-" + eleve.getNom() + "-" + eleve.getPrenom() + "-" + periode.getLibelle() + ".pdf";
                String chemin = minioStorageService.uploadBytes("bulletins", nomFichier, "application/pdf", pdf);

                Acte acte = new Acte();
                acte.setCode("BUL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
                acte.setReference(eleve.getNom() + " " + eleve.getPrenom() + " — " + classe.getLibelle() + " — " + periode.getLibelle());
                acte.setTypeActeId(typeBulletin.getId());
                acte.setEleveId(eleve.getId());
                acte.setPeriodeId(periodeId);
                acte.setCheminFichier(chemin);
                acte.setNomFichier(nomFichier);
                acteRepository.save(acte);
            } catch (Exception e) {
                log.error("Erreur archivage bulletin élève {} période {} : {}", eleve.getId(), periodeId, e.getMessage());
            }
        }
    }

    public ValidationBulletinResponse devalider(Long classeId, Long periodeId, String email) {
        validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId).ifPresent(vb -> {
            vb.setValide(false);
            validationBulletinRepository.save(vb);
        });
        progressionSaisieNoteService.devaliderToutesMatieresClasse(classeId, periodeId, email);
        return getStatut(classeId, periodeId);
    }
}
