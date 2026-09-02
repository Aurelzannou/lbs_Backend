package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.CaisseResponse;
import com.App.lbs_backend.entity.Caisse;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.mapper.CaisseMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.CaisseRepository;
import com.App.lbs_backend.service.UtilisateurSyncService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CaisseService extends AbstractBaseService<Caisse, CaisseResponse> {

    private final CaisseRepository caisseRepository;
    private final CaisseMapper caisseMapper;
    private final UtilisateurSyncService utilisateurSyncService;

    public CaisseService(CaisseRepository caisseRepository, CaisseMapper caisseMapper,
                         UtilisateurSyncService utilisateurSyncService) {
        super(Caisse.class);
        this.caisseRepository = caisseRepository;
        this.caisseMapper = caisseMapper;
        this.utilisateurSyncService = utilisateurSyncService;
    }

    @Override
    public BaseRepository<Caisse> repository() {
        return caisseRepository;
    }

    @Override
    public Mapper<Caisse, CaisseResponse> mapper() {
        return caisseMapper;
    }

    /** Caisse rattachée à l'utilisateur connecté (null s'il n'en a aucune) — utilisée pour
        pré-sélectionner automatiquement sa caisse sur l'écran d'encaissement. Priorité aux
        caisses actives. */
    public CaisseResponse getCaisseDeLUtilisateurConnecte() {
        Utilisateur user = utilisateurSyncService.getCurrentUser();
        if (user == null) return null;
        return caisseRepository.findByUtilisateurId(user.getId()).stream()
                .min(Comparator.comparing(c -> !Boolean.TRUE.equals(c.getActif())))
                .map(caisseMapper::toResponse)
                .orElse(null);
    }

    /** Empêche qu'une même personne soit responsable de deux caisses différentes. */
    public void verifierUtilisateurLibre(Long utilisateurId, Long caisseIdEnCours) {
        if (utilisateurId == null) return;
        List<Caisse> autres = caisseRepository.findByUtilisateurId(utilisateurId).stream()
                .filter(c -> !c.getId().equals(caisseIdEnCours))
                .toList();
        if (!autres.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cet utilisateur est déjà responsable de la caisse « " + autres.get(0).getLibelle() + " ».");
        }
    }
}
