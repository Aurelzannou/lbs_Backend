package com.App.lbs_backend.config;

import com.App.lbs_backend.entity.*;
import com.App.lbs_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

/**
 * Jeu de données de DÉMONSTRATION pour tester le module Comptabilité.
 *
 * Remplit les tables de référence encore vides : types de frais, frais scolaires
 * (par classe), échéanciers, modes de paiement, caisses, catégories de dépenses,
 * types d'opérations. Idempotent (repère par `code`) : peut tourner à chaque
 * démarrage sans créer de doublon.
 *
 * Activation : app.demo-data.enabled=true  (vrai en local, faux en prod).
 * Ne crée AUCune transaction (paiement / dépense) — seulement le référentiel,
 * pour que tous les écrans et listes déroulantes de la Comptabilité soient utilisables.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataLoader implements CommandLineRunner {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final ClasseRepository classeRepository;
    private final TypeFraisRepository typeFraisRepository;
    private final FraisScolaireRepository fraisScolaireRepository;
    private final EcheancierRepository echeancierRepository;
    private final ModePaiementRepository modePaiementRepository;
    private final CaisseRepository caisseRepository;
    private final CategorieDepenseRepository categorieDepenseRepository;
    private final TypeOperationRepository typeOperationRepository;

    @Value("${app.demo-data.enabled:false}")
    private boolean enabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        log.info("[demo-data] Initialisation du référentiel Comptabilité...");

        seedModesPaiement();
        seedCaisses();
        seedCategoriesDepense();
        seedTypesOperation();

        AnneeScolaire annee = anneeScolaireActive();
        if (annee == null) {
            log.warn("[demo-data] Aucune année scolaire — frais scolaires non générés.");
            log.info("[demo-data] Terminé (référentiel de caisse uniquement).");
            return;
        }

        TypeFraisRefs tf = seedTypesFrais();
        seedFraisScolaires(annee, tf);

        log.info("[demo-data] Terminé.");
    }

    // ─────────────────────────────────────────────────────────────
    //  Modes de paiement
    // ─────────────────────────────────────────────────────────────
    private void seedModesPaiement() {
        modePaiement("ESPECES", "Espèces");
        modePaiement("MOMO", "Mobile Money (MTN)");
        modePaiement("MOOV", "Moov Money");
        modePaiement("VIREMENT", "Virement bancaire");
        modePaiement("CHEQUE", "Chèque");
        modePaiement("DEPOT", "Dépôt bancaire");
    }

    private void modePaiement(String code, String libelle) {
        getOrCreate(code, () -> modePaiementRepository.findByCode(code).orElse(null), () -> {
            ModePaiement m = new ModePaiement();
            m.setCode(code);
            m.setLibelle(libelle);
            m.setActif(true);
            return modePaiementRepository.save(m);
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Caisses
    // ─────────────────────────────────────────────────────────────
    private void seedCaisses() {
        caisse("CAISSE-PRINCIPALE", "Caisse principale");
        caisse("CAISSE-ANNEXE", "Caisse annexe (petite caisse)");
    }

    private void caisse(String code, String libelle) {
        getOrCreate(code, () -> caisseRepository.findByCode(code).orElse(null), () -> {
            Caisse c = new Caisse();
            c.setCode(code);
            c.setLibelle(libelle);
            c.setSolde(0d);
            c.setActif(true);
            return caisseRepository.save(c);
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Catégories de dépense
    // ─────────────────────────────────────────────────────────────
    private void seedCategoriesDepense() {
        categorie("SALAIRES", "Salaires et rémunérations");
        categorie("LOYER", "Loyer et charges locatives");
        categorie("ELECTRICITE", "Électricité");
        categorie("EAU", "Eau");
        categorie("FOURNITURES-BUR", "Fournitures de bureau");
        categorie("MATERIEL-PEDAGO", "Matériel pédagogique");
        categorie("ENTRETIEN", "Entretien et réparations");
        categorie("TRANSPORT", "Transport et carburant");
        categorie("COMMUNICATION", "Communication (téléphone, internet)");
        categorie("EVENEMENTS", "Événements et sorties scolaires");
        categorie("IMPOTS-TAXES", "Impôts et taxes");
        categorie("DIVERS", "Dépenses diverses");
    }

    private void categorie(String code, String libelle) {
        getOrCreate(code, () -> categorieDepenseRepository.findByCode(code).orElse(null), () -> {
            CategorieDepense c = new CategorieDepense();
            c.setCode(code);
            c.setLibelle(libelle);
            c.setActif(true);
            return categorieDepenseRepository.save(c);
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Types d'opération
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
    //  Types de frais
    // ─────────────────────────────────────────────────────────────
    private record TypeFraisRefs(TypeFrais inscription, TypeFrais scolarite, TypeFrais cantine,
                                 TypeFrais tenue, TypeFrais assurance, TypeFrais examen,
                                 TypeFrais fournitures) {}

    private TypeFraisRefs seedTypesFrais() {
        return new TypeFraisRefs(
            typeFrais("INSCRIPTION", "Frais d'inscription", true),
            typeFrais("SCOLARITE", "Scolarité", true),
            typeFrais("CANTINE", "Cantine", false),
            typeFrais("TENUE", "Tenue scolaire", true),
            typeFrais("ASSURANCE", "Assurance scolaire", true),
            typeFrais("EXAMEN", "Frais d'examen", true),
            typeFrais("FOURNITURES", "Fournitures scolaires", false)
        );
    }

    private TypeFrais typeFrais(String code, String libelle, boolean obligatoire) {
        return typeFraisRepository.findByCode(code)
                .map(t -> (TypeFrais) t)
                .orElseGet(() -> {
                    log.info("[demo-data] type de frais : {}", code);
                    TypeFrais t = new TypeFrais();
                    t.setCode(code);
                    t.setLibelle(libelle);
                    t.setObligatoire(obligatoire);
                    t.setActif(true);
                    return typeFraisRepository.save(t);
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  Frais scolaires + échéanciers (par classe active)
    // ─────────────────────────────────────────────────────────────
    private void seedFraisScolaires(AnneeScolaire annee, TypeFraisRefs tf) {
        List<Classe> classes = classeRepository.findAll().stream()
                .filter(c -> !Boolean.FALSE.equals(c.getActif()))
                .toList();

        for (Classe classe : classes) {
            String cycle = cycleDe(classe.getCode());
            boolean classeExamen = estClasseExamen(classe.getCode());

            fraisScolaire(annee, classe, tf.inscription(), montantInscription(cycle), false);
            FraisScolaire scolarite =
                    fraisScolaire(annee, classe, tf.scolarite(), montantScolarite(cycle), false);
            fraisScolaire(annee, classe, tf.cantine(), 90_000d, false);
            fraisScolaire(annee, classe, tf.tenue(), 12_000d, false);
            fraisScolaire(annee, classe, tf.assurance(), 3_000d, false);
            fraisScolaire(annee, classe, tf.fournitures(), montantFournitures(cycle), false);
            if (classeExamen) {
                fraisScolaire(annee, classe, tf.examen(), 15_000d, false);
            }

            if (scolarite != null) {
                seedEcheancierScolarite(scolarite, annee);
            }
        }
    }

    private FraisScolaire fraisScolaire(AnneeScolaire annee, Classe classe, TypeFrais type,
                                        double montant, boolean force) {
        String code = "FRSC-" + classe.getCode() + "-" + type.getCode();
        return fraisScolaireRepository.findByCode(code)
                .map(f -> (FraisScolaire) f)
                .orElseGet(() -> {
                    FraisScolaire f = new FraisScolaire();
                    f.setCode(code);
                    f.setAnneeScolaireId(annee.getId());
                    f.setClasseId(classe.getId());
                    f.setTypeFraisId(type.getId());
                    f.setMontant(montant);
                    f.setActif(true);
                    return fraisScolaireRepository.save(f);
                });
    }

    private void seedEcheancierScolarite(FraisScolaire scolarite, AnneeScolaire annee) {
        if (!echeancierRepository.findByFraisScolaireIdOrderByNumeroAsc(scolarite.getId()).isEmpty()) {
            return; // déjà généré
        }
        LocalDate debut = annee.getDateDebut() != null ? annee.getDateDebut() : LocalDate.now();
        double total = scolarite.getMontant() != null ? scolarite.getMontant() : 0d;
        double tranche = Math.round(total / 3d);

        echeancier(scolarite, 1, "1re tranche", debut.plusMonths(1).withDayOfMonth(15), tranche);
        echeancier(scolarite, 2, "2e tranche", debut.plusMonths(4).withDayOfMonth(15), tranche);
        echeancier(scolarite, 3, "3e tranche", debut.plusMonths(7).withDayOfMonth(15), total - 2 * tranche);
    }

    private void echeancier(FraisScolaire frais, int numero, String libelle, LocalDate date, double montant) {
        Echeancier e = new Echeancier();
        e.setCode("ECH-" + frais.getCode() + "-T" + numero);
        e.setFraisScolaireId(frais.getId());
        e.setNumero(numero);
        e.setLibelle(libelle);
        e.setDateEcheance(date);
        e.setMontant(montant);
        echeancierRepository.save(e);
    }

    // ─────────────────────────────────────────────────────────────
    //  Barème par cycle (FCFA)
    // ─────────────────────────────────────────────────────────────
    private double montantInscription(String cycle) {
        return switch (cycle) {
            case "MATERNELLE", "PRIMAIRE" -> 15_000d;
            case "COLLEGE" -> 20_000d;
            default -> 25_000d; // LYCEE
        };
    }

    private double montantScolarite(String cycle) {
        return switch (cycle) {
            case "MATERNELLE" -> 120_000d;
            case "PRIMAIRE" -> 150_000d;
            case "COLLEGE" -> 210_000d;
            default -> 270_000d; // LYCEE
        };
    }

    private double montantFournitures(String cycle) {
        return switch (cycle) {
            case "MATERNELLE" -> 25_000d;
            case "PRIMAIRE" -> 30_000d;
            default -> 20_000d;
        };
    }

    private String cycleDe(String classeCode) {
        if (classeCode == null) return "PRIMAIRE";
        String c = classeCode.toUpperCase();
        if (c.startsWith("PS") || c.startsWith("MS") || c.startsWith("GS") || c.startsWith("CI")) return "MATERNELLE";
        if (c.startsWith("CP") || c.startsWith("CE") || c.startsWith("CM")) return "PRIMAIRE";
        if (c.startsWith("6") || c.startsWith("5") || c.startsWith("4") || c.startsWith("3")) return "COLLEGE";
        return "LYCEE";
    }

    private boolean estClasseExamen(String classeCode) {
        if (classeCode == null) return false;
        String c = classeCode.toUpperCase();
        return c.startsWith("CM2") || c.startsWith("3EME") || c.startsWith("TLE") || c.startsWith("TERM");
    }

    // ─────────────────────────────────────────────────────────────
    //  Utilitaires
    // ─────────────────────────────────────────────────────────────
    private AnneeScolaire anneeScolaireActive() {
        List<AnneeScolaire> annees = anneeScolaireRepository.findAll();
        return annees.stream()
                .filter(a -> Boolean.TRUE.equals(a.getActif()))
                .findFirst()
                .orElse(annees.isEmpty() ? null : annees.get(0));
    }

    private <T> void getOrCreate(String code, Supplier<T> finder, Supplier<T> creator) {
        if (finder.get() == null) {
            log.info("[demo-data] création : {}", code);
            creator.get();
        }
    }
}
