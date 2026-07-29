package com.App.lbs_backend.config;

import com.App.lbs_backend.entity.Menu;
import com.App.lbs_backend.entity.Profil;
import com.App.lbs_backend.entity.ProfilMenu;
import com.App.lbs_backend.entity.ProfilUtilisateur;
import com.App.lbs_backend.entity.StatutInscription;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.repository.MenuRepository;
import com.App.lbs_backend.repository.ProfilRepository;
import com.App.lbs_backend.repository.ProfilUtilisateurRepository;
import com.App.lbs_backend.repository.StatutInscriptionRepository;
import com.App.lbs_backend.repository.UtilisateurRepository;
import com.App.lbs_backend.service.KeycloakAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final StatutInscriptionRepository statutInscriptionRepository;
    private final KeycloakAdminService keycloakAdminService;
    private final UtilisateurRepository utilisateurRepository;
    private final ProfilUtilisateurRepository profilUtilisateurRepository;

    @Value("${superadmin.username}")
    private String superAdminUsername;

    @Value("${superadmin.email}")
    private String superAdminEmail;

    @Value("${superadmin.password}")
    private String superAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Vérification et initialisation des données de base...");

        // 0. Synchronisation avec Keycloak - Créer les rôles s'ils n'existent pas
        keycloakAdminService.createRole("ADMIN", "Administrateur Système");
        keycloakAdminService.createRole("TUTEUR", "Parent / Tuteur");
        keycloakAdminService.createRole("PROFESSEUR", "Professeur");

        // 1. Initialisation des Profils (DB locale)
        Profil admin = createProfilIfNotFound("ADMIN", "Administrateur Système");
        createProfilIfNotFound("TUTEUR", "Parent / Tuteur");
        createProfilIfNotFound("PROFESSEUR", "Professeur");

        // 1bis. Compte super-admin par défaut, pour pouvoir administrer dès le premier démarrage
        createSuperAdminIfNotFound(admin);

        // 2. Initialisation des Menus
        log.info("Vérification des menus par défaut...");
        
        // Dashboard
        Menu dashboard = createOrUpdateMenu("DASHBOARD", "Tableau de bord", "Vue d'ensemble", "home-outline", "/dashboard", 1, List.of(admin));

        // Profil
        Menu profil = createOrUpdateMenu("PROFIL", "Mon Profil", "Gérer mon profil", "person-outline", "/profile", 2, List.of(admin));
        
        // Administration (Parent)
        Menu adminGroup = createOrUpdateMenu("ADMINISTRATION", "Administration", "Gestion du système", "shield-outline", null, 3, List.of(admin));
        
        // Sous-menus Administration
        createOrUpdateSubMenu("USER", "Utilisateurs", "Gestion des utilisateurs", "people-outline", "/administration/utilisateurs", 1, adminGroup, admin);
        createOrUpdateSubMenu("PROFIL_ADMIN", "Profils", "Gestion des profils", "lock-outline", "/administration/profils", 2, adminGroup, admin);
        createOrUpdateSubMenu("MENU_ADMIN", "Menus", "Gestion des menus", "menu-outline", "/administration/menus", 3, adminGroup, admin);

        // Référentiel (Parent)
        Menu refGroup = createOrUpdateMenu("REFERENTIEL", "Référentiel", "Données de référence", "settings-2-outline", null, 4, List.of(admin));
        createOrUpdateSubMenu("NIVEAUX", "Niveaux Scolaires", "Gestion des niveaux", "layers-outline", "/referentiel/niveaux", 1, refGroup, admin);
        createOrUpdateSubMenu("PERIODE_ACADEMIQUE", "Périodes académiques", "Gestion des trimestres", "calendar-outline", "/referentiel/periodes-academiques", 2, refGroup, admin);
        createOrUpdateSubMenu("ANNEE_SCOLAIRE", "Années Scolaires", "Gestion des années scolaires", "calendar-outline", "/referentiel/annees-scolaires", 3, refGroup, admin);
        createOrUpdateSubMenu("PERIODE_INSCRIPTION", "Périodes d'inscription", "Gestion des périodes d'inscription", "calendar-check-outline", "/referentiel/periodes-inscription", 4, refGroup, admin);
        createOrUpdateSubMenu("ETAPES", "Étapes", "Gestion des étapes du dossier", "timer-outline", "/referentiel/etapes", 5, refGroup, admin);
        createOrUpdateSubMenu("CLASSES", "Classes", "Gestion des classes", "grid-outline", "/referentiel/classes", 6, refGroup, admin);
        createOrUpdateSubMenu("PROFESSEURS", "Professeurs", "Gestion des professeurs", "briefcase-outline", "/referentiel/professeurs", 7, refGroup, admin);
        createOrUpdateSubMenu("MATIERES", "Matières", "Gestion des matières", "book-outline", "/referentiel/matieres", 8, refGroup, admin);
        createOrUpdateSubMenu("COEFFICIENTS", "Coefficients", "Gestion des coefficients", "calculator-outline", "/referentiel/coefficients", 9, refGroup, admin);
        createOrUpdateSubMenu("CAISSES", "Caisses", "Gestion des caisses", "wallet-outline", "/referentiel/caisses", 10, refGroup, admin);
        createOrUpdateSubMenu("CATEGORIES_DEPENSES", "Catégories de dépenses", "Gestion des catégories de dépenses", "folder-outline", "/referentiel/categories-depenses", 11, refGroup, admin);
        createOrUpdateSubMenu("FRAIS_SCOLAIRES", "Frais scolaires", "Gestion des frais scolaires", "cash-outline", "/referentiel/frais-scolaires", 12, refGroup, admin);
        createOrUpdateSubMenu("MODES_PAIEMENT", "Modes de paiement", "Gestion des modes de paiement", "credit-card-outline", "/referentiel/modes-paiements", 13, refGroup, admin);
        createOrUpdateSubMenu("TYPES_ACTES", "Types d'actes", "Gestion des types d'actes", "file-text-outline", "/referentiel/types-actes", 14, refGroup, admin);
        createOrUpdateSubMenu("TYPES_FRAIS", "Types de frais", "Gestion des types de frais", "pricetags-outline", "/referentiel/types-frais", 15, refGroup, admin);
        createOrUpdateSubMenu("TYPES_OPERATIONS", "Types d'opérations", "Gestion des types d'opérations", "swap-outline", "/referentiel/types-operations", 16, refGroup, admin);
        createOrUpdateSubMenu("STATUTS_INSCRIPTIONS", "Statuts d'inscription", "Gestion des statuts d'inscription", "flag-outline", "/referentiel/statuts-inscriptions", 17, refGroup, admin);

        // Scolarité (Parent)
        Menu scolariteGroup = createOrUpdateMenu("SCOLARITE", "Scolarité", "Gestion scolaire", "book-open-outline", null, 5, List.of(admin));
        createOrUpdateSubMenu("ELEVES", "Élèves", "Gestion des élèves", "people-outline", "/scolarite/eleves", 1, scolariteGroup, admin);
        createOrUpdateSubMenu("INSCRIPTIONS", "Inscriptions", "Gestion des dossiers d'inscription", "file-text-outline", "/scolarite/inscriptions", 2, scolariteGroup, admin);
        createOrUpdateSubMenu("VALIDATIONS", "Validations", "Validation des dossiers d'inscription", "checkmark-circle-outline", "/scolarite/validations", 3, scolariteGroup, admin);

        // Gestion des emplois du temps (menu autonome, pas un sous-menu)
        createOrUpdateMenu("EMPLOI_DU_TEMPS", "Gestion des emplois du temps", "Planification des cours par classe", "clock-outline", "/emploi-du-temps", 6, List.of(admin));

        // Présences (menu autonome, pas un sous-menu)
        createOrUpdateMenu("PRESENCES", "Présences", "Suivi des présences élèves et professeurs", "clipboard-outline", "/presences", 7, List.of(admin));

        // 6ter. Menu Notes (autonome, premier niveau)
        Menu notesGroup = createOrUpdateMenu("NOTES", "Notes", "Gestion des notes et bulletins", "award-outline", null, 8, List.of(admin));
        createOrUpdateSubMenu("SAISIE_NOTES", "Saisie des notes", "Saisie des notes par classe et matière", "edit-2-outline", "/notes/saisie", 1, notesGroup, admin);
        createOrUpdateSubMenu("VALIDATION_BULLETINS", "Validation des bulletins", "Validation et impression des bulletins", "checkmark-square-2-outline", "/notes/validation", 2, notesGroup, admin);

        // 3. Statuts d'inscription
        log.info("Vérification des statuts d'inscription...");
        createStatutIfNotFound("DEPOSE",     "Déposé");
        createStatutIfNotFound("EN_ATTENTE", "En attente");
        createStatutIfNotFound("ACCEPTE",    "Accepté");
        createStatutIfNotFound("REFUSE",     "Refusé");
        createStatutIfNotFound("INSCRIT",    "Inscrit");
        createStatutIfNotFound("ANNULE",     "Annulé");

        log.info("Initialisation des données de base terminée.");
    }

    private void createStatutIfNotFound(String code, String libelle) {
        statutInscriptionRepository.findByCode(code).orElseGet(() -> {
            log.info("Création du statut inscription : {}", code);
            StatutInscription statut = new StatutInscription();
            statut.setCode(code);
            statut.setLibelle(libelle);
            return statutInscriptionRepository.save(statut);
        });
    }

    private void createSuperAdminIfNotFound(Profil adminProfil) {
        if (utilisateurRepository.existsByLogin(superAdminUsername)) {
            return;
        }
        log.warn("Création du compte super-admin par défaut '{}' — pensez à changer son mot de passe en production.", superAdminUsername);

        String keycloakId = keycloakAdminService.createUser(
                superAdminUsername, superAdminEmail, "Super", "Admin", superAdminPassword);

        Utilisateur user = new Utilisateur();
        user.setKeycloack(keycloakId);
        user.setLogin(superAdminUsername);
        user.setEmail(superAdminEmail);
        user.setNom("Admin");
        user.setPrenom("Super");
        utilisateurRepository.save(user);

        ProfilUtilisateur pu = new ProfilUtilisateur();
        pu.setUtilisateurId(user.getId());
        pu.setProfilId(adminProfil.getId());
        pu.setCode(user.getLogin() + "_" + adminProfil.getCode() + "_" + System.currentTimeMillis());
        profilUtilisateurRepository.save(pu);

        try {
            keycloakAdminService.assignRoleToUser(keycloakId, "ADMIN");
        } catch (Exception e) {
            log.error("Erreur assignation rôle ADMIN au super-admin : {}", e.getMessage());
        }

        log.info("Compte super-admin créé : login='{}'", superAdminUsername);
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

    private Menu createOrUpdateMenu(String code, String titre, String description, String icon, String path, int ordre, List<Profil> profils) {
        Menu menu = menuRepository.findByCode(code).orElseGet(() -> {
            log.info("Création du menu : {}", code);
            Menu newMenu = new Menu();
            newMenu.setCode(code);
            return newMenu;
        });

        menu.setTitre(titre);
        menu.setDescription(description);
        menu.setIcon(icon);
        menu.setPath(path);
        menu.setOrdre(ordre);
        
        // Gestion des associations de profil (éviter les doublons)
        for (Profil p : profils) {
            boolean alreadyLinked = menu.getListeProfilMenu().stream()
                    .anyMatch(pm -> pm.getProfil() != null && p.getCode().equals(pm.getProfil().getCode()));
            
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

    private void createOrUpdateSubMenu(String code, String titre, String description, String icon, String path, int ordre, Menu parent, Profil profil) {
        Menu subMenu = menuRepository.findByCode(code).orElseGet(() -> {
            log.info("Création du sous-menu : {}", code);
            Menu newSubMenu = new Menu();
            newSubMenu.setCode(code);
            return newSubMenu;
        });

        subMenu.setTitre(titre);
        subMenu.setDescription(description);
        subMenu.setIcon(icon);
        subMenu.setPath(path);
        subMenu.setOrdre(ordre);
        subMenu.setMenuEnfantId(parent.getId());
        
        // Gestion de l'association (éviter les doublons)
        boolean alreadyLinked = subMenu.getListeProfilMenu().stream()
                .anyMatch(pm -> pm.getProfil() != null && profil.getCode().equals(pm.getProfil().getCode()));
        
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
