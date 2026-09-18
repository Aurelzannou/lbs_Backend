package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.http.response.MetaResponse;
import com.App.lbs_backend.core.http.response.PageResponse;
import com.App.lbs_backend.core.utils.ReportService;
import com.App.lbs_backend.dto.response.EleveListeLignePdf;
import com.App.lbs_backend.dto.response.EleveResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.mapper.EleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.EleveRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EleveService extends AbstractBaseService<Eleve, EleveResponse> {

    private final EleveRepository eleveRepository;
    private final EleveMapper eleveMapper;
    private final ClasseRepository classeRepository;
    private final AnneeScolaireRepository anneeScolaireRepository;
    private final ReportService reportService;

    public EleveService(EleveRepository eleveRepository, EleveMapper eleveMapper,
                        ClasseRepository classeRepository, AnneeScolaireRepository anneeScolaireRepository,
                        ReportService reportService) {
        super(Eleve.class);
        this.eleveRepository = eleveRepository;
        this.eleveMapper = eleveMapper;
        this.classeRepository = classeRepository;
        this.anneeScolaireRepository = anneeScolaireRepository;
        this.reportService = reportService;
    }

    @Override
    public BaseRepository<Eleve> repository() {
        return eleveRepository;
    }

    @Override
    public Mapper<Eleve, EleveResponse> mapper() {
        return eleveMapper;
    }

    /**
     * Recherche paginée avec filtre texte + classe, restreignable à une année scolaire (un élève
     * n'appartient à une année que via un dossier « vivant » — ni refusé ni annulé — pour cette
     * année ; `anneeScolaireId` null = toutes années confondues).
     */
    public Page<Eleve> searchFiltered(Long classeId, String filter, Long anneeScolaireId, Pageable pageable) {
        return eleveRepository.searchFiltered(classeId, filter, anneeScolaireId, pageable);
    }

    /** Convertit une Page JPA en PageResponse DTO. */
    public PageResponse<EleveResponse> toPageResponse(Page<Eleve> page) {
        List<EleveResponse> items = page.getContent().stream()
                .map(e -> mapper().toResponse(e))
                .collect(Collectors.toList());
        return new PageResponse<>(items, MetaResponse.ofPage(page));
    }

    /** Combine recherche + mapping dans UNE seule transaction : le mapper accède à des relations
        LAZY (classe, utilisateur) qui nécessitent une session Hibernate ouverte — appeler
        searchFiltered() puis toPageResponse() séparément échoue en prod (open-in-view=false) dès
        que ces relations sont renseignées. */
    @Transactional(readOnly = true)
    public PageResponse<EleveResponse> searchFilteredResponse(Long classeId, String filter, Long anneeScolaireId, Pageable pageable) {
        return toPageResponse(searchFiltered(classeId, filter, anneeScolaireId, pageable));
    }

    /**
     * PDF « Liste des élèves » d'une classe : tous les élèves de la classe, triés par nom puis
     * prénom, numérotés. Sert de liste de classe imprimable (appel, trombinoscope papier…).
     */
    @Transactional(readOnly = true)
    public byte[] genererListeClassePdf(Long classeId) {
        List<Eleve> eleves = eleveRepository.findByClasseIdOrderByNomAscPrenomAsc(classeId);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<EleveListeLignePdf> lignes = new ArrayList<>();
        int i = 1;
        for (Eleve e : eleves) {
            String nom = e.getNom() != null ? e.getNom().toUpperCase() : "";
            String prenom = e.getPrenom() != null ? e.getPrenom() : "";
            lignes.add(new EleveListeLignePdf(
                    i++,
                    (nom + " " + prenom).trim(),
                    "M".equalsIgnoreCase(e.getSexe()) ? "M" : "F",
                    e.getDateNaissance() != null ? df.format(e.getDateNaissance()) : "—"));
        }

        String classeLibelle = classeRepository.findById(classeId)
                .map(c -> c.getLibelle() + (c.getCode() != null ? " (" + c.getCode() + ")" : ""))
                .orElse("Classe #" + classeId);
        String anneeLibelle = anneeScolaireRepository.findFirstByActifTrue()
                .map(AnneeScolaire::getLibelle).orElse("—");

        Map<String, Object> params = new HashMap<>();
        params.put("classe", classeLibelle);
        params.put("anneeScolaire", anneeLibelle);
        params.put("effectif", String.valueOf(lignes.size()));
        params.put("dateGeneration", LocalDate.now().format(df));

        try {
            return reportService.generatePdfReport("liste-eleves-classe", params, lignes);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer la liste des élèves : " + e.getMessage(), e);
        }
    }
}
