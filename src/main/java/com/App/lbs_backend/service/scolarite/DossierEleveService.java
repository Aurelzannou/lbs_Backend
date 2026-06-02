package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.dto.response.DossierEleveResponse;
import com.App.lbs_backend.entity.DossierEleve;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.StatutInscription;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.mapper.DossierEleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.DossierEleveRepository;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DossierEleveService extends AbstractBaseService<DossierEleve, DossierEleveResponse> {

    private final DossierEleveRepository dossierEleveRepository;
    private final DossierEleveMapper     dossierEleveMapper;
    private final StatutInscriptionRepository statutRepository;
    private final EleveRepository        eleveRepository;
    private final TuteurRepository       tuteurRepository;
    private final EmailService           emailService;

    public DossierEleveService(DossierEleveRepository dossierEleveRepository,
                               DossierEleveMapper dossierEleveMapper,
                               StatutInscriptionRepository statutRepository,
                               EleveRepository eleveRepository,
                               TuteurRepository tuteurRepository,
                               EmailService emailService) {
        super(DossierEleve.class);
        this.dossierEleveRepository = dossierEleveRepository;
        this.dossierEleveMapper     = dossierEleveMapper;
        this.statutRepository       = statutRepository;
        this.eleveRepository        = eleveRepository;
        this.tuteurRepository       = tuteurRepository;
        this.emailService           = emailService;
    }

    @Override
    public BaseRepository<DossierEleve> repository() { return dossierEleveRepository; }

    @Override
    public Mapper<DossierEleve, DossierEleveResponse> mapper() { return dossierEleveMapper; }

    public void setStatutDepose(DossierEleve dossier) {
        statutRepository.findByCode("DEPOSE")
                .ifPresent(s -> dossier.setStatutId(s.getId()));
    }

    @Transactional
    public DossierEleveResponse changerStatut(String uuid, String statutCode) {
        DossierEleve dossier = findByUuid(uuid);
        StatutInscription statut = statutRepository.findByCode(statutCode)
                .orElseThrow(() -> new RuntimeException("Statut non trouvé : " + statutCode));
        dossier.setStatutId(statut.getId());
        update(dossier);
        DossierEleveResponse response = toResponse(dossier.getId());

        // Envoyer notification email au parent
        envoyerEmailNotification(dossier, statutCode);

        return response;
    }

    public List<DossierEleveResponse> getByTuteurId(Long tuteurId) {
        return dossierEleveRepository.findByTuteurId(tuteurId).stream()
                .map(d -> mapper().toResponse(d))
                .collect(Collectors.toList());
    }

    private void envoyerEmailNotification(DossierEleve dossier, String statutCode) {
        if (!List.of("ACCEPTE", "REFUSE").contains(statutCode)) return;

        try {
            Eleve eleve = eleveRepository.findById(dossier.getEleveId()).orElse(null);
            if (eleve == null || eleve.getTuteurId() == null) return;

            Tuteur tuteur = tuteurRepository.findById(eleve.getTuteurId()).orElse(null);
            if (tuteur == null || tuteur.getEmail() == null) return;

            String classe       = dossier.getClasse() != null ? dossier.getClasse().getLibelle() : "—";
            String anneeScolaire = dossier.getAnneeScolaire() != null ? dossier.getAnneeScolaire().getLibelle() : "—";
            String numero       = dossier.getNumero() != null ? dossier.getNumero() : "—";

            if ("ACCEPTE".equals(statutCode)) {
                emailService.sendDossierAccepte(
                    tuteur.getEmail(),
                    tuteur.getNom(), tuteur.getPrenom(),
                    eleve.getNom(), eleve.getPrenom(),
                    classe, anneeScolaire, numero
                );
            } else {
                emailService.sendDossierRefuse(
                    tuteur.getEmail(),
                    tuteur.getNom(), tuteur.getPrenom(),
                    eleve.getNom(), eleve.getPrenom(),
                    classe, anneeScolaire, numero, null
                );
            }
        } catch (Exception e) {
            log.error("Erreur envoi notification email pour dossier {} : {}", dossier.getUuid(), e.getMessage());
        }
    }
}
