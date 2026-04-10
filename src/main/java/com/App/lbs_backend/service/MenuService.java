package com.App.lbs_backend.service;

import com.App.lbs_backend.dto.request.MenuRequest;
import com.App.lbs_backend.entity.Menu;
import com.App.lbs_backend.entity.ProfilMenu;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.mapper.MenuMapper;
import com.App.lbs_backend.repository.MenuRepository;
import com.App.lbs_backend.repository.ProfilRepository;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UtilisateurSyncService utilisateurSyncService;
    private final MenuMapper menuMapper;

    /**
     * Retourne la liste des menus autorisés pour l'utilisateur connecté.
     * - Si l'utilisateur a le rôle Keycloak ROLE_ADMIN → tous les menus
     * - Sinon : menus filtrés par les profils locaux de l'utilisateur
     */
    public List<Menu> getMyMenus() {
        Utilisateur currentUser = utilisateurSyncService.getCurrentUser();

        // Vérifier si l'utilisateur est admin via le rôle Keycloak
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_admin"));

        if (isAdmin) {
            log.info("Utilisateur ADMIN détecté ({}), chargement de tous les menus.", currentUser.getLogin());
            // Retourne tous les menus de premier niveau (les enfants sont chargés par Hibernate via listeMenuEnfant)
            return menuRepository.findAll().stream()
                    .filter(m -> m.getMenuEnfantId() == null)
                    .sorted(Comparator.comparing(m -> m.getOrdre() != null ? m.getOrdre() : 0))
                    .collect(Collectors.toList());
        }

        // 1. Récupérer les IDs des profils de l'utilisateur
        List<Long> profilIds = profilUtilisateurRepository.findByUtilisateurId(currentUser.getId())
                .stream()
                .map(ProfilUtilisateur::getProfilId)
                .collect(Collectors.toList());

        if (profilIds.isEmpty()) {
            log.warn("L'utilisateur {} n'a aucun profil assigné.", currentUser.getLogin());
            return List.of();
        }

        // 2. Récupérer les menus racines associés aux profils de l'utilisateur
        return menuRepository.findDistinctByListeProfilMenu_Profil_IdInOrderByOrdreAsc(profilIds).stream()
                .filter(m -> m.getMenuEnfantId() == null)
                .collect(Collectors.toList());
    }

    /**
     * Retourne tous les menus pour l'administration (liste plate)
     */
    public List<Menu> getAll() {
        return menuRepository.findAll();
    }

    public Menu getById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu non trouvé avec l'id : " + id));
    }

    public Menu create(MenuRequest request) {
        Menu menu = new Menu();
        menuMapper.updateEntity(menu, request);

        if (request.getProfilIds() != null && !request.getProfilIds().isEmpty()) {
            for (Long profilId : request.getProfilIds()) {
                ProfilMenu pm = new ProfilMenu();
                pm.setProfilId(profilId);
                pm.setMenu(menu);
                menu.getListeProfilMenu().add(pm);
            }
        }

        return menuRepository.save(menu);
    }

    public Menu update(Long id, MenuRequest request) {
        Menu menu = getById(id);
        menuMapper.updateEntity(menu, request);

        if (request.getProfilIds() != null) {
            menu.getListeProfilMenu().clear();
            for (Long profilId : request.getProfilIds()) {
                ProfilMenu pm = new ProfilMenu();
                pm.setProfilId(profilId);
                pm.setMenu(menu);
                menu.getListeProfilMenu().add(pm);
            }
        }

        return menuRepository.save(menu);
    }

    public void delete(Long id) {
        Menu menu = getById(id);
        menuRepository.delete(menu);
    }
}
