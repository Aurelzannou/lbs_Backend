package com.App.lbs_backend.config;

import com.App.lbs_backend.entity.Menu;
import com.App.lbs_backend.entity.Profil;
import com.App.lbs_backend.entity.ProfilMenu;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.repository.MenuRepository;
import com.App.lbs_backend.repository.ProfilRepository;
import com.App.lbs_backend.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialDataLoader implements CommandLineRunner {

    private final ProfilRepository profilRepository;
    private final MenuRepository menuRepository;
    private final KeycloakAdminService keycloakAdminService;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Vérification et initialisation des données de base...");

        // 0. Synchronisation avec Keycloak - Créer les rôles s'ils n'existent pas
        keycloakAdminService.createRole("ADMIN", "Administrateur Système");
        keycloakAdminService.createRole("LECTEUR", "Lecteur (Consultation)");

        // 1. Initialisation des Profils (DB locale)
        Profil admin = createProfilIfNotFound("ADMIN", "Administrateur Système");
        Profil lecteur = createProfilIfNotFound("LECTEUR", "Lecteur (Consultation)");

        // 2. Initialisation des Menus
        log.info("Vérification des menus par défaut...");
        
        // Dashboard
        Menu dashboard = createOrUpdateMenu("DASHBOARD", "Tableau de bord", "home-outline", "/dashboard", 1, List.of(admin, lecteur));
        
        // Profil
        Menu profil = createOrUpdateMenu("PROFIL", "Mon Profil", "person-outline", "/profile", 2, List.of(admin, lecteur));
        
        // Administration (Parent)
        Menu adminGroup = createOrUpdateMenu("ADMINISTRATION", "Administration", "shield-outline", null, 3, List.of(admin));
        
        // Sous-menus Administration
        createOrUpdateSubMenu("USER", "Utilisateurs", "people-outline", "/administration/utilisateurs", 1, adminGroup, admin);
        createOrUpdateSubMenu("PROFIL_ADMIN", "Profils", "lock-outline", "/administration/profils", 2, adminGroup, admin);
        createOrUpdateSubMenu("MENU_ADMIN", "Menus", "menu-outline", "/administration/menus", 3, adminGroup, admin);
        
        log.info("Initialisation des données de base terminée.");
    }

    private Profil createProfilIfNotFound(String code, String libelle) {
        return profilRepository.findByCode(code).orElseGet(() -> {
            log.info("Création du profil : {}", code);
            Profil profil = new Profil();
            profil.setCode(code);
            profil.setLibelle(libelle);
            return profilRepository.save(profil);
        });
    }

    private Menu createOrUpdateMenu(String code, String titre, String description, String path, int ordre, List<Profil> profils) {
        Menu menu = menuRepository.findByCode(code).orElseGet(() -> {
            log.info("Création du menu : {}", code);
            Menu newMenu = new Menu();
            newMenu.setCode(code);
            return newMenu;
        });

        menu.setTitre(titre);
        menu.setDescription(description); 
        menu.setPath(path);
        menu.setOrdre(ordre);
        
        // Gestion des associations de profil (éviter les doublons)
        for (Profil p : profils) {
            boolean alreadyLinked = menu.getListeProfilMenu().stream()
                    .anyMatch(pm -> p.getCode().equals(pm.getProfil().getCode()));
            
            if (!alreadyLinked) {
                log.info("Liaison du menu {} au profil {}", code, p.getCode());
                ProfilMenu pm = new ProfilMenu();
                pm.setProfil(p);
                pm.setMenu(menu);
                menu.getListeProfilMenu().add(pm);
            }
        }
        
        return menuRepository.save(menu);
    }

    private void createOrUpdateSubMenu(String code, String titre, String description, String path, int ordre, Menu parent, Profil profil) {
        Menu subMenu = menuRepository.findByCode(code).orElseGet(() -> {
            log.info("Création du sous-menu : {}", code);
            Menu newSubMenu = new Menu();
            newSubMenu.setCode(code);
            return newSubMenu;
        });

        subMenu.setTitre(titre);
        subMenu.setDescription(description);
        subMenu.setPath(path);
        subMenu.setOrdre(ordre);
        subMenu.setMenuEnfantId(parent.getId());
        
        // Gestion de l'association (éviter les doublons)
        boolean alreadyLinked = subMenu.getListeProfilMenu().stream()
                .anyMatch(pm -> profil.getCode().equals(pm.getProfil().getCode()));
        
        if (!alreadyLinked) {
            log.info("Liaison du sous-menu {} au profil {}", code, profil.getCode());
            ProfilMenu pm = new ProfilMenu();
            pm.setProfil(profil);
            pm.setMenu(subMenu);
            subMenu.getListeProfilMenu().add(pm);
        }
        
        menuRepository.save(subMenu);
    }
}
