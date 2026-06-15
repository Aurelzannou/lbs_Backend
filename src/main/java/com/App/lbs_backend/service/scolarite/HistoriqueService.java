package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.response.HistoriqueDossierResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.HistoriqueDossier;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.HistoriqueDossierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoriqueService {

    private final HistoriqueDossierRepository historiqueRepository;
    private final DossierEleveRepository      dossierRepository;

    /** Enregistre une action dans l'historique d'un dossier. */
    public void enregistrer(Long dossierId, String action, String effectuePar, String commentaire) {
        HistoriqueDossier h = new HistoriqueDossier();
        h.setDossierId(dossierId);
        h.setAction(action);
        h.setEffectuePar(effectuePar);
        h.setCommentaire(commentaire);
        historiqueRepository.save(h);
    }

    /** Retourne l'historique complet d'un dossier identifié par son UUID. */
    public List<HistoriqueDossierResponse> getByDossierUuid(String uuid) {
        DossierEleve dossier = dossierRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable : " + uuid));
        return historiqueRepository.findByDossierIdOrderByEffectueLeDesc(dossier.getId())
                .stream()
                .map(h -> new HistoriqueDossierResponse(
                        h.getId(), h.getAction(), h.getEffectuePar(),
                        h.getEffectueLe(), h.getCommentaire()))
                .collect(Collectors.toList());
    }
}
