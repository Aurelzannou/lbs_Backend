package com.App.lbs_backend.service;

import com.App.lbs_backend.entity.Menu;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.repository.MenuRepository;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final UtilisateurSyncService utilisateurSyncService;

    /**
     * Retourne la liste des menus autorisés pour l'utilisateur connecté.
     * Les menus sont filtrés par ses profils et structurés par ordre.
     */
    public List<Menu> getMyMenus() {
        Utilisateur currentUser = utilisateurSyncService.getCurrentUser();
        
        // 1. Récupérer les IDs des profils de l'utilisateur
        List<Long> profilIds = profilUtilisateurRepository.findByUtilisateurId(currentUser.getId())
                .stream()
                .map(ProfilUtilisateur::getProfilId)
                .collect(Collectors.toList());

        if (profilIds.isEmpty()) {
            log.warn("L'utilisateur {} n'a aucun profil assigné.", currentUser.getLogin());
            return List.of();
        }

        // 2. Récupérer les menus associés à ces profils
        // On récupère uniquement les menus racines (ceux qui n'ont pas de parent)
        // Les enfants seront chargés par le mapper (ou via Hibernate fetch)
        List<Menu> allMenus = menuRepository.findDistinctByListeProfil_IdInOrderByOrdreAsc(profilIds);
        
        // 3. Ne retourner que les menus de premier niveau
        return allMenus.stream()
                .filter(m -> m.getMenuEnfantId() == null)
                .collect(Collectors.toList());
    }
}
