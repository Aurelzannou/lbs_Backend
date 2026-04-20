package com.App.lbs_backend.service;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.request.MenuRequest;
import com.App.lbs_backend.dto.response.MenuResponse;
import com.App.lbs_backend.entity.Menu;
import com.App.lbs_backend.entity.ProfilMenu;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.mapper.MenuMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.MenuRepository;
import com.App.lbs_backend.repository.ProfilRepository;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MenuService extends AbstractBaseService<Menu, MenuResponse> {

    private final MenuRepository menuRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;
    private final ProfilRepository profilRepository;
    private final UtilisateurSyncService utilisateurSyncService;
    private final MenuMapper menuMapper;

    public MenuService(MenuRepository menuRepository, 
                       ProfilUtilisateurRepository profilUtilisateurRepository, 
                       ProfilRepository profilRepository, 
                       UtilisateurSyncService utilisateurSyncService, 
                       MenuMapper menuMapper) {
        super(Menu.class);
        this.menuRepository = menuRepository;
        this.profilUtilisateurRepository = profilUtilisateurRepository;
        this.profilRepository = profilRepository;
        this.utilisateurSyncService = utilisateurSyncService;
        this.menuMapper = menuMapper;
    }

    @Override
    public BaseRepository<Menu> repository() {
        return menuRepository;
    }

    @Override
    public Mapper<Menu, MenuResponse> mapper() {
        return menuMapper;
    }

    /**
     * Retourne la liste des menus autorisés pour l'utilisateur connecté.
     */
    public List<Menu> getMyMenus() {
        Utilisateur currentUser = utilisateurSyncService.getCurrentUser();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_admin"));

        if (isAdmin) {
            log.info("Utilisateur ADMIN détecté ({}), chargement de tous les menus.", currentUser.getLogin());
            return menuRepository.findAll().stream()
                    .filter(m -> m.getMenuEnfantId() == null)
                    .sorted(Comparator.comparing(m -> m.getOrdre() != null ? m.getOrdre() : 0))
                    .collect(Collectors.toList());
        }

        List<Long> profilIds = profilUtilisateurRepository.findByUtilisateurId(currentUser.getId())
                .stream()
                .map(ProfilUtilisateur::getProfilId)
                .collect(Collectors.toList());

        if (profilIds.isEmpty()) {
            return List.of();
        }

        return menuRepository.findDistinctByListeProfilMenu_Profil_IdInOrderByOrdreAsc(profilIds).stream()
                .filter(m -> m.getMenuEnfantId() == null)
                .collect(Collectors.toList());
    }

    @Transactional
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

    @Transactional
    public Menu update(Long id, MenuRequest request) {
        Menu menu = findById(id);
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

    @Transactional
    public boolean delete(Long id) {
        Menu menu = findById(id);
        menuRepository.delete(menu);
        return true;
    }
}
