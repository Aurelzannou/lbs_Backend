package com.App.lbs_backend.service;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.request.MenuRequest;
import com.App.lbs_backend.dto.response.MenuResponse;
import com.App.lbs_backend.entity.Menu;
import com.App.lbs_backend.entity.Profil;
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
import java.util.Set;
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
     * Arbre des menus autorisés pour l'utilisateur connecté. Le filtrage par profil s'applique
     * AUSSI aux sous-menus : un caissier ne voit sous « Comptabilité » que les écrans qui lui
     * sont explicitement autorisés, pas tous les enfants du groupe.
     */
    public List<MenuResponse> getMyMenusTree(String profilCode) {
        Set<Long> allowed = allowedMenuIds(profilCode); // null = accès total (admin)

        List<Menu> all = menuRepository.findAll();

        return all.stream()
                .filter(m -> m.getMenuEnfantId() == null)
                .filter(m -> allowed == null || allowed.contains(m.getId()))
                .sorted(Comparator.comparing(m -> m.getOrdre() != null ? m.getOrdre() : 0))
                .map(parent -> {
                    List<MenuResponse> enfants = all.stream()
                            .filter(c -> parent.getId().equals(c.getMenuEnfantId()))
                            .filter(c -> allowed == null || allowed.contains(c.getId()))
                            .sorted(Comparator.comparing(c -> c.getOrdre() != null ? c.getOrdre() : 0))
                            .map(c -> menuMapper.toResponse(c, List.of()))
                            .collect(Collectors.toList());
                    return menuMapper.toResponse(parent, enfants);
                })
                // Un menu-GROUPE (sans chemin propre) devenu vide après filtrage est masqué.
                .filter(mr -> (mr.path() != null && !mr.path().isBlank())
                        || (mr.listeMenuEnfant() != null && !mr.listeMenuEnfant().isEmpty()))
                .collect(Collectors.toList());
    }

    /** IDs des menus visibles : {@code null} = tout (admin), sinon les menus liés aux profils
        concernés (un profil précis, ou l'union de tous les profils de l'utilisateur). */
    private Set<Long> allowedMenuIds(String profilCode) {
        Utilisateur currentUser = utilisateurSyncService.getCurrentUser();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasAdminRole = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_admin"));

        if (hasAdminRole && (profilCode == null || "ADMIN".equalsIgnoreCase(profilCode))) {
            return null; // accès total
        }

        List<ProfilUtilisateur> liens = profilUtilisateurRepository.findByUtilisateurId(currentUser.getId());
        List<Long> profilIds;
        if (profilCode != null && !profilCode.isBlank()) {
            profilIds = liens.stream()
                    .map(pu -> profilRepository.findById(pu.getProfilId()).orElse(null))
                    .filter(p -> p != null && profilCode.equalsIgnoreCase(p.getCode()))
                    .map(Profil::getId)
                    .collect(Collectors.toList());
        } else {
            profilIds = liens.stream().map(ProfilUtilisateur::getProfilId).collect(Collectors.toList());
        }

        if (profilIds.isEmpty()) return Set.of();

        return menuRepository.findDistinctByListeProfilMenu_Profil_IdInOrderByOrdreAsc(profilIds).stream()
                .map(Menu::getId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public Menu create(MenuRequest request) {
        Menu menu = new Menu();
        menuMapper.updateEntity(menu, request);

        if (request.getProfilIds() != null && !request.getProfilIds().isEmpty()) {
            for (Long profilId : request.getProfilIds()) {
                menu.getListeProfilMenu().add(lierProfil(menu, profilId));
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
                menu.getListeProfilMenu().add(lierProfil(menu, profilId));
            }
        }

        return menuRepository.save(menu);
    }

    /** La colonne lbs_prme_profil_id est en lecture seule sur le champ {@code profilId} :
        l'écriture doit passer par la relation {@code profil}, sinon la ligne est enregistrée
        avec un profil NULL. */
    private ProfilMenu lierProfil(Menu menu, Long profilId) {
        var profil = profilRepository.findById(profilId)
                .orElseThrow(() -> new IllegalArgumentException("Profil introuvable : " + profilId));
        ProfilMenu pm = new ProfilMenu();
        pm.setProfil(profil);
        pm.setMenu(menu);
        return pm;
    }

    @Transactional
    public boolean delete(Long id) {
        Menu menu = findById(id);
        menuRepository.delete(menu);
        return true;
    }
}
