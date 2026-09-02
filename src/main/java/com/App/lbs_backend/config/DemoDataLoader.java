package com.App.lbs_backend.config;

import com.App.lbs_backend.entity.Etape;
import com.App.lbs_backend.entity.TypeFrais;
import com.App.lbs_backend.entity.TypeOperation;
import com.App.lbs_backend.repository.EtapeRepository;
import com.App.lbs_backend.repository.TypeFraisRepository;
import com.App.lbs_backend.repository.TypeOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Données de référence minimales chargées au démarrage :
 *  - les étapes du workflow d'inscription (BROUILLON / SOUMISE / VALIDEE)
 *  - les types d'opération de caisse (ENCAISSEMENT / DECAISSEMENT / ...)
 *
 * Idempotent (repère par `code`) : ne crée jamais de doublon et ne supprime rien.
 * Activation : app.demo-data.enabled=true (par défaut en local, false en prod).
 *
 * Toutes les autres données (années, niveaux, classes, frais, élèves, caisses,
 * modes de paiement, catégories de dépense…) se saisissent via l'application.
 */
@Component
@Order(2) // après InitialDataLoader (profils, statuts, superadmin)
@RequiredArgsConstructor
@Slf4j
public class DemoDataLoader implements CommandLineRunner {

    private final EtapeRepository etapeRepository;
    private final TypeOperationRepository typeOperationRepository;
    private final TypeFraisRepository typeFraisRepository;

    @Value("${app.demo-data.enabled:false}")
    private boolean enabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        log.info("[demo-data] Initialisation des données de référence minimales...");

        seedEtapes();
        seedTypesOperation();
        seedTypesFrais();

        log.info("[demo-data] Terminé.");
    }

    // ─────────────────────────────────────────────────────────────
    //  Étapes du dossier d'inscription
    // ─────────────────────────────────────────────────────────────
    private void seedEtapes() {
        etape("BROUILLON", "Brouillon");
        etape("SOUMISE", "Soumise pour validation");
        etape("VALIDEE", "Validée");
    }

    private void etape(String code, String libelle) {
        if (etapeRepository.findByCode(code).isPresent()) return;
        log.info("[demo-data] étape : {}", code);
        Etape e = new Etape();
        e.setCode(code);
        e.setLibelle(libelle);
        e.setActif(true);
        etapeRepository.save(e);
    }

    // ─────────────────────────────────────────────────────────────
    //  Types d'opération de caisse
    // ─────────────────────────────────────────────────────────────
    private void seedTypesOperation() {
        typeOperation("ENCAISSEMENT", "Encaissement", "Entrée d'argent en caisse (paiement de frais)");
        typeOperation("DECAISSEMENT", "Décaissement", "Sortie d'argent de caisse (dépense)");
        typeOperation("TRANSFERT", "Transfert entre caisses", "Déplacement de fonds d'une caisse vers une autre");
        typeOperation("APPRO", "Approvisionnement", "Alimentation d'une caisse (dépôt initial, apport)");
        typeOperation("AJUSTEMENT", "Ajustement", "Correction de solde après inventaire de caisse");
    }

    private void typeOperation(String code, String libelle, String description) {
        getOrCreate(code, () -> typeOperationRepository.findByCode(code).orElse(null), () -> {
            TypeOperation t = new TypeOperation();
            t.setCode(code);
            t.setLibelle(libelle);
            t.setDescription(description);
            return typeOperationRepository.save(t);
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Types de frais :
    //   - INSCRIPTION : requis par le paiement FedaPay en ligne
    //     (voir PaiementInscriptionService / fedapay.type-frais-inscription).
    //   - SCOLARITE : seul type encaissé au guichet sur /comptabilite/paiements
    //     (voir app.paiement.type-frais-code).
    //  Ajouter d'autres lignes ici si besoin (CANTINE, TRANSPORT, ...).
    // ─────────────────────────────────────────────────────────────
    private void seedTypesFrais() {
        typeFrais("INSCRIPTION", "Frais d'inscription", true);
        typeFrais("SCOLARITE", "Scolarité", true);
    }

    private void typeFrais(String code, String libelle, boolean obligatoire) {
        getOrCreate(code, () -> typeFraisRepository.findByCode(code).orElse(null), () -> {
            TypeFrais t = new TypeFrais();
            t.setCode(code);
            t.setLibelle(libelle);
            t.setObligatoire(obligatoire);
            t.setActif(true);
            return typeFraisRepository.save(t);
        });
    }

    // ─────────────────────────────────────────────────────────────
    private <T> void getOrCreate(String code, Supplier<T> finder, Supplier<T> creator) {
        if (finder.get() == null) {
            log.info("[demo-data] création : {}", code);
            creator.get();
        }
    }
}
