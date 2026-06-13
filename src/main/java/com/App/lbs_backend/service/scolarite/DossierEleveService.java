package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.mapper.DossierEleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service CRUD pour les dossiers d'inscription.
 * Gère aussi la génération du numéro de dossier (INS-ANNÉE-NOM3PRENOM1-SÉQUENCE).
 */
@Service
public class DossierEleveService extends AbstractBaseService<DossierEleve, DossierEleveResponse> {

    private final DossierEleveRepository      dossierEleveRepository;
    private final DossierEleveMapper          dossierEleveMapper;
    private final StatutInscriptionRepository statutRepository;

    public DossierEleveService(DossierEleveRepository dossierEleveRepository,
                               DossierEleveMapper dossierEleveMapper,
                               StatutInscriptionRepository statutRepository) {
        super(DossierEleve.class);
        this.dossierEleveRepository = dossierEleveRepository;
        this.dossierEleveMapper     = dossierEleveMapper;
        this.statutRepository       = statutRepository;
    }

    @Override
    public BaseRepository<DossierEleve> repository() { return dossierEleveRepository; }

    @Override
    public Mapper<DossierEleve, DossierEleveResponse> mapper() { return dossierEleveMapper; }

    /** Auto-set du statut DEPOSE lors de la création d'un dossier. */
    public void setStatutDepose(DossierEleve dossier) {
        statutRepository.findByCode("DEPOSE")
                .ifPresent(s -> dossier.setStatutId(s.getId()));
    }

    /**
     * Génère un numéro de dossier unique : INS-{ANNÉE}-{NOM3}{PRENOM1}-{SÉQUENCE 4 chiffres}
     * Exemple : INS-2026-COMJ-0001
     */
    public String genererNumero(String nom, String prenom) {
        int annee = LocalDate.now().getYear();
        String trigramme = buildTrigramme(nom, prenom);
        long count = dossierEleveRepository.countByAnnee(annee);
        return "INS-" + annee + "-" + trigramme + "-" + String.format("%04d", count + 1);
    }

    /** Retourne les dossiers d'un tuteur. */
    public List<DossierEleveResponse> getByTuteurId(Long tuteurId) {
        return dossierEleveRepository.findByTuteurId(tuteurId).stream()
                .map(d -> mapper().toResponse(d))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String buildTrigramme(String nom, String prenom) {
        String n = normalize(nom);
        String p = normalize(prenom);
        String part1 = n.length() >= 3 ? n.substring(0, 3) : n;
        String part2 = !p.isEmpty() ? p.substring(0, 1) : "";
        return (part1 + part2).toUpperCase();
    }

    private String normalize(String s) {
        if (s == null || s.isBlank()) return "X";
        return s.trim()
                .replaceAll("[^a-zA-ZÀ-ÿ]", "")
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]",   "e")
                .replaceAll("[ìíîï]",   "i")
                .replaceAll("[òóôõö]",  "o")
                .replaceAll("[ùúûü]",   "u")
                .replaceAll("[ç]",      "c");
    }
}
