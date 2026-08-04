package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.request.FeuillePresenceRequest;
import com.App.lbs_backend.dto.response.FeuillePresenceResponse;
import com.App.lbs_backend.dto.response.PresenceEnfantResponse;
import com.App.lbs_backend.dto.response.PresenceHistoriqueResponse;
import com.App.lbs_backend.dto.response.SeanceJourResponse;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.entity.EmploiDuTemps;
import com.App.lbs_backend.entity.PresenceEleve;
import com.App.lbs_backend.entity.PresenceProfesseur;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.repository.EleveRepository;
import com.App.lbs_backend.repository.EmploiDuTempsRepository;
import com.App.lbs_backend.repository.PresenceEleveRepository;
import com.App.lbs_backend.repository.PresenceProfesseurRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import com.App.lbs_backend.service.referentiel.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private static final String STATUT_PAR_DEFAUT = "PRESENT";

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final EleveRepository eleveRepository;
    private final PresenceEleveRepository presenceEleveRepository;
    private final PresenceProfesseurRepository presenceProfesseurRepository;
    private final EmploiDuTempsService emploiDuTempsService;
    private final TuteurRepository tuteurRepository;

    /** Convertit une date en nom de jour (LUNDI..SAMEDI) tel que stocké sur EmploiDuTemps. */
    public static String jourDeLaSemaine(LocalDate date) {
        DayOfWeek jour = date.getDayOfWeek();
        return switch (jour) {
            case MONDAY -> "LUNDI";
            case TUESDAY -> "MARDI";
            case WEDNESDAY -> "MERCREDI";
            case THURSDAY -> "JEUDI";
            case FRIDAY -> "VENDREDI";
            case SATURDAY -> "SAMEDI";
            case SUNDAY -> "DIMANCHE";
        };
    }

    /** Liste des séances du jour de la semaine correspondant à `date`, avec l'indication si la présence a déjà été prise. */
    public List<SeanceJourResponse> getSeancesDuJour(Long classeId, Long anneeScolaireId, LocalDate date) {
        String jour = jourDeLaSemaine(date);
        List<EmploiDuTemps> seances = emploiDuTempsRepository
                .findByClasseIdAndAnneeScolaireIdAndJourOrderByHeureDebutAsc(classeId, anneeScolaireId, jour);

        return seances.stream().map(e -> {
            SeanceJourResponse dto = new SeanceJourResponse();
            dto.setEmploiTempsId(e.getId());
            dto.setJour(e.getJour());
            dto.setHeureDebut(e.getHeureDebut());
            dto.setHeureFin(e.getHeureFin());
            if (e.getMatiere() != null) dto.setMatiereLibelle(e.getMatiere().getLibelle());
            if (e.getProfesseur() != null) {
                dto.setProfNomComplet(e.getProfesseur().getNom() + " " + e.getProfesseur().getPrenom());
            }
            dto.setPresenceEnregistree(
                    presenceProfesseurRepository.findByEmploiTempsIdAndDate(e.getId(), date).isPresent());
            return dto;
        }).collect(Collectors.toList());
    }

    /** Charge la feuille de présence (roster élèves + statut prof) pour un créneau et une date donnés. */
    public FeuillePresenceResponse getFeuille(Long emploiTempsId, LocalDate date) {
        EmploiDuTemps seance = emploiDuTempsRepository.findById(emploiTempsId)
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable"));

        List<Eleve> eleves = eleveRepository.findByClasseIdOrderByNomAscPrenomAsc(seance.getClasseId());

        Map<Long, String> statutsExistants = presenceEleveRepository
                .findByEmploiTempsIdAndDate(emploiTempsId, date).stream()
                .collect(Collectors.toMap(PresenceEleve::getEleveId, PresenceEleve::getStatut));

        String profStatut = presenceProfesseurRepository.findByEmploiTempsIdAndDate(emploiTempsId, date)
                .map(PresenceProfesseur::getStatut)
                .orElse(STATUT_PAR_DEFAUT);

        FeuillePresenceResponse response = new FeuillePresenceResponse();
        response.setEmploiTempsId(seance.getId());
        response.setDate(date);
        response.setJour(seance.getJour());
        response.setHeureDebut(seance.getHeureDebut());
        response.setHeureFin(seance.getHeureFin());
        if (seance.getClasse() != null) response.setClasseLibelle(seance.getClasse().getLibelle());
        if (seance.getMatiere() != null) response.setMatiereLibelle(seance.getMatiere().getLibelle());
        response.setProfId(seance.getProfId());
        if (seance.getProfesseur() != null) {
            response.setProfNomComplet(seance.getProfesseur().getNom() + " " + seance.getProfesseur().getPrenom());
        }
        response.setProfStatut(profStatut);

        response.setEleves(eleves.stream().map(el -> {
            FeuillePresenceResponse.EleveStatutDto dto = new FeuillePresenceResponse.EleveStatutDto();
            dto.setEleveId(el.getId());
            dto.setNom(el.getNom());
            dto.setPrenom(el.getPrenom());
            dto.setStatut(statutsExistants.getOrDefault(el.getId(), STATUT_PAR_DEFAUT));
            return dto;
        }).collect(Collectors.toList()));

        return response;
    }

    /** Enregistre (upsert) la feuille de présence d'un créneau/date : élèves + professeur. */
    @Transactional
    public FeuillePresenceResponse enregistrerFeuille(FeuillePresenceRequest form) {
        EmploiDuTemps seance = emploiDuTempsRepository.findById(form.getEmploiTempsId())
                .orElseThrow(() -> new IllegalArgumentException("Créneau introuvable"));

        emploiDuTempsService.verifierAnneeModifiable(seance.getAnneeScolaireId());

        PresenceProfesseur presenceProf = presenceProfesseurRepository
                .findByEmploiTempsIdAndDate(form.getEmploiTempsId(), form.getDate())
                .orElseGet(PresenceProfesseur::new);
        presenceProf.setEmploiTempsId(form.getEmploiTempsId());
        presenceProf.setDate(form.getDate());
        presenceProf.setStatut(form.getProfStatut());
        if (presenceProf.getCode() == null) {
            presenceProf.setCode("PRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        }
        presenceProfesseurRepository.save(presenceProf);

        for (FeuillePresenceRequest.EleveStatut es : form.getEleves()) {
            PresenceEleve presenceEleve = presenceEleveRepository
                    .findByEmploiTempsIdAndDateAndEleveId(form.getEmploiTempsId(), form.getDate(), es.getEleveId())
                    .orElseGet(PresenceEleve::new);
            presenceEleve.setEmploiTempsId(form.getEmploiTempsId());
            presenceEleve.setDate(form.getDate());
            presenceEleve.setEleveId(es.getEleveId());
            presenceEleve.setStatut(es.getStatut());
            if (presenceEleve.getCode() == null) {
                presenceEleve.setCode("PRE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
            }
            presenceEleveRepository.save(presenceEleve);
        }

        return getFeuille(form.getEmploiTempsId(), form.getDate());
    }

    /** Historique de présence d'un élève (toutes séances confondues), le plus récent en premier. */
    public List<PresenceHistoriqueResponse> getHistoriqueEleve(Long eleveId) {
        return presenceEleveRepository.findByEleveIdOrderByDateDesc(eleveId).stream().map(p -> {
            PresenceHistoriqueResponse dto = new PresenceHistoriqueResponse();
            dto.setDate(p.getDate());
            dto.setStatut(p.getStatut());
            EmploiDuTemps seance = p.getEmploiDuTemps();
            if (seance != null) {
                dto.setJour(seance.getJour());
                dto.setHeureDebut(seance.getHeureDebut());
                dto.setHeureFin(seance.getHeureFin());
                if (seance.getMatiere() != null) dto.setMatiereLibelle(seance.getMatiere().getLibelle());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /** Historique de présence de tous les enfants du tuteur connecté (identifié par son email). */
    public List<PresenceEnfantResponse> getPresencesMesEnfants(String email) {
        Tuteur tuteur = tuteurRepository.findByEmail(email).orElse(null);
        if (tuteur == null) return Collections.emptyList();

        List<Eleve> enfants = eleveRepository.findByTuteurId(tuteur.getId());
        return enfants.stream().map(enfant -> {
            PresenceEnfantResponse dto = new PresenceEnfantResponse();
            dto.setEleveId(enfant.getId());
            dto.setEleveNomComplet(enfant.getNom() + " " + enfant.getPrenom());
            dto.setHistorique(getHistoriqueEleve(enfant.getId()));
            return dto;
        }).collect(Collectors.toList());
    }
}
