package com.App.lbs_backend.controller.scolarite;

import com.App.lbs_backend.core.http.response.ApiResponse;
import com.App.lbs_backend.core.utils.ReportService;
import com.App.lbs_backend.dto.request.BulletinMentionRequest;
import com.App.lbs_backend.dto.response.BulletinResponse;
import com.App.lbs_backend.service.scolarite.BulletinService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/bulletins")
@RequiredArgsConstructor
public class BulletinController {

    private final BulletinService bulletinService;
    private final ReportService reportService;
    private final HttpServletRequest httpRequest;

    @GetMapping("/eleve/{eleveId}")
    public ResponseEntity<?> getBulletinEleve(@PathVariable Long eleveId, @RequestParam Long periodeId) {
        BulletinResponse bulletin = bulletinService.genererBulletin(eleveId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", bulletin, httpRequest.getRequestURI()));
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<?> getBulletinsClasse(@PathVariable Long classeId, @RequestParam Long periodeId) {
        List<BulletinResponse> bulletins = bulletinService.genererBulletinsClasse(classeId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", bulletins, httpRequest.getRequestURI()));
    }

    @PutMapping("/mentions/{eleveId}/{periodeId}")
    public ResponseEntity<?> enregistrerMentions(
            @PathVariable Long eleveId, @PathVariable Long periodeId, @RequestBody BulletinMentionRequest form) {
        bulletinService.enregistrerMentions(eleveId, periodeId, form);
        BulletinResponse bulletin = bulletinService.genererBulletin(eleveId, periodeId);
        return ResponseEntity.ok(ApiResponse.apiSuccess("Mentions enregistrées", bulletin, httpRequest.getRequestURI()));
    }

    @GetMapping("/mes-enfants/{eleveId}")
    public ResponseEntity<?> getBulletinPourTuteur(
            @PathVariable Long eleveId, @RequestParam Long periodeId, @AuthenticationPrincipal Jwt jwt) {
        String email = extraireEmail(jwt);
        if (email == null) {
            return ResponseEntity.status(403).body(ApiResponse.apiError("Non authentifié", httpRequest.getRequestURI()));
        }
        BulletinResponse bulletin = bulletinService.genererBulletinPourTuteur(eleveId, periodeId, email);
        return ResponseEntity.ok(ApiResponse.apiSuccess("OK", bulletin, httpRequest.getRequestURI()));
    }

    @GetMapping("/eleve/{eleveId}/pdf")
    public ResponseEntity<byte[]> getBulletinEleveePdf(@PathVariable Long eleveId, @RequestParam Long periodeId) {
        BulletinResponse bulletin = bulletinService.genererBulletin(eleveId, periodeId);
        try {
            byte[] pdf = reportService.generatePdfReport("bulletin", buildParams(bulletin), bulletin.getMatieres());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"bulletin-" + eleveId + "-" + periodeId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Erreur génération PDF bulletin élève {} période {}", eleveId, periodeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/classe/{classeId}/pdf")
    public ResponseEntity<byte[]> getBulletinsClassePdf(@PathVariable Long classeId, @RequestParam Long periodeId) {
        List<BulletinResponse> bulletins = bulletinService.genererBulletinsClasse(classeId, periodeId);
        try {
            List<JasperPrint> prints = new ArrayList<>();
            for (BulletinResponse b : bulletins) {
                prints.add(reportService.fillReport("bulletin", buildParams(b), b.getMatieres()));
            }
            byte[] pdf = reportService.exportToPdf(prints);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"bulletins-classe-" + classeId + "-" + periodeId + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Erreur génération PDF bulletins classe {} période {}", classeId, periodeId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private Map<String, Object> buildParams(BulletinResponse b) {
        Map<String, Object> params = new HashMap<>();
        params.put("eleveNomComplet", b.getEleveNomComplet());
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
        return params;
    }

    private String formatMoyenne(Double valeur) {
        return valeur != null ? String.format(Locale.FRANCE, "%.2f", valeur) : "—";
    }

    private String formatRang(Integer rang) {
        return rang != null ? rang + "e" : "—";
    }

    private String formatOuiNon(Boolean valeur) {
        return Boolean.TRUE.equals(valeur) ? "Oui" : "Non";
    }

    private String extraireEmail(Jwt jwt) {
        if (jwt == null) return null;
        String email = jwt.getClaimAsString("email");
        return email != null ? email : jwt.getClaimAsString("preferred_username");
    }
}
