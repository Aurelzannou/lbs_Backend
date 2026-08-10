package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.dto.request.FeuilleSaisieNotesRequest;
import com.App.lbs_backend.dto.request.ProgressionMatiereRequest;
import com.App.lbs_backend.dto.request.VerrouProgressionRequest;
import com.App.lbs_backend.dto.response.ClasseMatiereANoterResponse;
import com.App.lbs_backend.dto.response.FeuilleSaisieNotesResponse;
import com.App.lbs_backend.dto.response.ProgressionSaisieNoteResponse;
import com.App.lbs_backend.entity.Professeur;
import com.App.lbs_backend.repository.ProfesseurRepository;
import com.App.lbs_backend.service.scolarite.NoteService;
import com.App.lbs_backend.service.scolarite.ProgressionSaisieNoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final ProgressionSaisieNoteService progressionSaisieNoteService;
    private final ProfesseurRepository professeurRepository;
    private final HttpServletRequest httpRequest;

    @GetMapping("/feuille")
    public ResponseEntity<?> getFeuille(
            @RequestParam Long classeId, @RequestParam Long matiereId, @RequestParam Long periodeId) {
        FeuilleSaisieNotesResponse feuille = noteService.getFeuille(classeId, matiereId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", feuille, httpRequest.getRequestURI()));
    }

    @PutMapping("/feuille")
    public ResponseEntity<?> enregistrerFeuille(
            @RequestBody FeuilleSaisieNotesRequest form, @AuthenticationPrincipal Jwt jwt) {
        // Si l'appelant est un professeur, on ne fait jamais confiance au professeurId envoyé par
        // le client pour l'autorisation — on le re-dérive du JWT connecté.
        String email = extraireEmail(jwt);
        if (email != null) {
            professeurRepository.findByEmail(email).ifPresent(p -> form.setProfesseurId(p.getId()));
        }
        FeuilleSaisieNotesResponse feuille = noteService.enregistrerFeuille(form);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Notes enregistrées", feuille, httpRequest.getRequestURI()));
    }

    @GetMapping("/mes-classes")
    public ResponseEntity<?> getMesClasses(@AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        if (email == null) return ResponseEntity.ok(ApiResponse.apiSuccess("OK", Collections.emptyList(), httpRequest.getRequestURI()));

        List<ClasseMatiereANoterResponse> classes = professeurRepository.findByEmail(email)
                .map(p -> noteService.getMesClassesANoter(p.getId()))
                .orElse(Collections.emptyList());
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", classes, httpRequest.getRequestURI()));
    }

    @GetMapping("/progression")
    public ResponseEntity<?> getProgression(
            @RequestParam Long classeId, @RequestParam Long matiereId, @RequestParam Long periodeId) {
        ProgressionSaisieNoteResponse dto = progressionSaisieNoteService.getProgression(classeId, matiereId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", dto, httpRequest.getRequestURI()));
    }

    /** Étape de chaque matière de la classe pour cette période — alimente l'écran admin
        "Validation des bulletins" (une matière encore en BROUILLON ne peut pas y être ouverte). */
    @GetMapping("/progression/classe")
    public ResponseEntity<?> getProgressionsClasse(@RequestParam Long classeId, @RequestParam Long periodeId) {
        List<ProgressionSaisieNoteResponse> liste = progressionSaisieNoteService.getProgressionsClasse(classeId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", liste, httpRequest.getRequestURI()));
    }

    /** Le professeur connecté verrouille la prochaine colonne (interrogation ou devoir) de sa
        classe/matière — plus personne ne pourra la modifier tant que l'admin ne la déverrouille pas. */
    @PutMapping("/progression/verrouiller")
    public ResponseEntity<?> verrouillerColonne(@RequestBody VerrouProgressionRequest form, @AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        Professeur professeur = email != null ? professeurRepository.findByEmail(email).orElse(null) : null;
        if (professeur == null) {
            return ResponseEntity.status(403).body(ApiResponse.apiError("Réservé aux professeurs", httpRequest.getRequestURI()));
        }
        ProgressionSaisieNoteResponse dto = "DEVOIR".equals(form.getTypeEvaluation())
                ? progressionSaisieNoteService.verrouillerDevoir(form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), form.getNumero(), professeur.getId())
                : progressionSaisieNoteService.verrouillerInterrogation(form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), form.getNumero(), professeur.getId());
        return ResponseEntity.ok(ApiResponse.apiSuccess("Colonne verrouillée", dto, httpRequest.getRequestURI()));
    }

    /** Réservé à l'admin côté frontend (aucun compte professeur n'affiche ce bouton) — permet de
        corriger un verrouillage posé par erreur, en reculant d'une colonne. */
    @PutMapping("/progression/deverrouiller")
    public ResponseEntity<?> deverrouillerColonne(@RequestBody VerrouProgressionRequest form) {
        ProgressionSaisieNoteResponse dto = "DEVOIR".equals(form.getTypeEvaluation())
                ? progressionSaisieNoteService.deverrouillerDevoir(form.getClasseId(), form.getMatiereId(), form.getPeriodeId())
                : progressionSaisieNoteService.deverrouillerInterrogation(form.getClasseId(), form.getMatiereId(), form.getPeriodeId());
        return ResponseEntity.ok(ApiResponse.apiSuccess("Colonne déverrouillée", dto, httpRequest.getRequestURI()));
    }

    /** Réservé à l'admin côté frontend — valide la colonne verrouillée en attente (débloque la
        colonne suivante pour le professeur). */
    @PutMapping("/progression/valider-colonne")
    public ResponseEntity<?> validerColonne(@RequestBody VerrouProgressionRequest form) {
        ProgressionSaisieNoteResponse dto = "DEVOIR".equals(form.getTypeEvaluation())
                ? progressionSaisieNoteService.validerDevoir(form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), form.getNumero())
                : progressionSaisieNoteService.validerInterrogation(form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), form.getNumero());
        return ResponseEntity.ok(ApiResponse.apiSuccess("Colonne validée", dto, httpRequest.getRequestURI()));
    }

    /** Le professeur connecté soumet la matière entière pour validation admin — nécessite que
        toutes les colonnes soient déjà verrouillées. */
    @PutMapping("/progression/soumettre")
    public ResponseEntity<?> soumettreMatiere(@RequestBody ProgressionMatiereRequest form, @AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        Professeur professeur = email != null ? professeurRepository.findByEmail(email).orElse(null) : null;
        if (professeur == null) {
            return ResponseEntity.status(403).body(ApiResponse.apiError("Réservé aux professeurs", httpRequest.getRequestURI()));
        }
        ProgressionSaisieNoteResponse dto = progressionSaisieNoteService.soumettre(
                form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), professeur.getId());
        return ResponseEntity.ok(ApiResponse.apiSuccess("Matière soumise pour validation", dto, httpRequest.getRequestURI()));
    }

    /** Réservé à l'admin côté frontend — valide la matière soumise par le professeur. */
    @PutMapping("/progression/valider")
    public ResponseEntity<?> validerMatiere(@RequestBody ProgressionMatiereRequest form, @AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        ProgressionSaisieNoteResponse dto = progressionSaisieNoteService.valider(
                form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), email);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Matière validée", dto, httpRequest.getRequestURI()));
    }

    /** Réservé à l'admin côté frontend — annule la validation (repasse à SOUMISE). */
    @PutMapping("/progression/devalider-matiere")
    public ResponseEntity<?> devaliderMatiere(@RequestBody ProgressionMatiereRequest form, @AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        ProgressionSaisieNoteResponse dto = progressionSaisieNoteService.devaliderMatiere(
                form.getClasseId(), form.getMatiereId(), form.getPeriodeId(), email);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Validation annulée", dto, httpRequest.getRequestURI()));
    }

    /** Historique complet des transitions d'étape (BROUILLON/SOUMISE/VALIDEE) de cette matière. */
    @GetMapping("/progression/historique")
    public ResponseEntity<?> getHistorique(
            @RequestParam Long classeId, @RequestParam Long matiereId, @RequestParam Long periodeId) {
        var historique = progressionSaisieNoteService.getHistorique(classeId, matiereId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", historique, httpRequest.getRequestURI()));
    }

    private String extraireEmail(Jwt jwt) {
        if (jwt == null) return null;
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getClaimAsString("preferred_username");
    }
}
