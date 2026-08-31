package com.App.lbs_backend.config;

import com.App.lbs_backend.entity.*;
import com.App.lbs_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
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
@Order(2) // après InitialDataLoader (profils, statuts, superadmin)
@RequiredArgsConstructor
@Slf4j
public class DemoDataLoader implements CommandLineRunner {

    private final AnneeScolaireRepository anneeScolaireRepository;
    private final ClasseRepository classeRepository;
    private final NiveauRepository niveauRepository;
    private final EtapeRepository etapeRepository;
    private final StatutInscriptionRepository statutInscriptionRepository;
    private final TuteurRepository tuteurRepository;
    private final EleveRepository eleveRepository;
    private final DossierEleveRepository dossierEleveRepository;
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
        log.info("[demo-data] Initialisation du jeu de données de démonstration...");

        // 1. Référentiel structurel (prérequis des frais / dossiers)
        seedAnneesScolaires();
        seedEtapes();
        seedNiveauxEtClasses();

        // 2. Référentiel comptable
        seedModesPaiement();
        seedCaisses();
        seedCategoriesDepense();
        seedTypesOperation();

        AnneeScolaire annee = anneeScolaireActive();
        if (annee == null) {
            log.warn("[demo-data] Aucune année scolaire — frais et dossiers non générés.");
            log.info("[demo-data] Terminé (référentiel de caisse uniquement).");
            return;
        }

        // 3. Frais scolaires + échéanciers par classe
        TypeFraisRefs tf = seedTypesFrais();
        seedFraisScolaires(annee, tf);

        // 4. Élèves + dossiers de démo (pour tester Paiements / Suivi / Journal)
        seedElevesEtDossiers(annee);

        log.info("[demo-data] Terminé.");
    }

    // ─────────────────────────────────────────────────────────────
    //  Années scolaires
    // ─────────────────────────────────────────────────────────────
    private void seedAnneesScolaires() {
        anneeScolaire("2026-2027", "Année scolaire 2026-2027",
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 7, 31), true);
        anneeScolaire("2027-2028", "Année scolaire 2027-2028",
                LocalDate.of(2027, 9, 1), LocalDate.of(2028, 7, 31), false);
    }

    private void anneeScolaire(String code, String libelle, LocalDate debut, LocalDate fin, boolean actif) {
        if (anneeScolaireRepository.findByCode(code).isPresent()) return;
        log.info("[demo-data] année scolaire : {}", code);
        AnneeScolaire a = new AnneeScolaire();
        a.setCode(code);
        a.setLibelle(libelle);
        a.setDateDebut(debut);
        a.setDateFin(fin);
        a.setActif(actif);
        anneeScolaireRepository.save(a);
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
    //  Niveaux + classes (une classe "A" par niveau)
    // ─────────────────────────────────────────────────────────────
    private static final String[][] NIVEAUX = {
        {"PS", "Petite section"}, {"MS", "Moyenne section"}, {"GS", "Grande section"},
        {"CI", "Cours d'initiation"},
        {"CP", "Cours préparatoire"}, {"CE1", "Cours élémentaire 1"}, {"CE2", "Cours élémentaire 2"},
        {"CM1", "Cours moyen 1"}, {"CM2", "Cours moyen 2"},
        {"6EME", "Sixième"}, {"5EME", "Cinquième"}, {"4EME", "Quatrième"}, {"3EME", "Troisième"},
        {"2NDE", "Seconde"}, {"1ERE", "Première"}, {"TLE", "Terminale"}
    };

    private void seedNiveauxEtClasses() {
        for (String[] n : NIVEAUX) {
            Niveau niveau = (Niveau) niveauRepository.findByCode(n[0]).orElseGet(() -> {
                log.info("[demo-data] niveau : {}", n[0]);
                Niveau x = new Niveau();
                x.setCode(n[0]);
                x.setLibelle(n[1]);
                return niveauRepository.save(x);
            });

            String classeCode = n[0] + "-A";
            if (classeRepository.findByCode(classeCode).isEmpty()) {
                log.info("[demo-data] classe : {}", classeCode);
                Classe c = new Classe();
                c.setCode(classeCode);
                c.setLibelle(n[1] + " A");
                c.setNiveauId(niveau.getId());
                c.setCapaciteMax(40);
                c.setActif(true);
                classeRepository.save(c);
            }
        }
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

            fraisScolaire(annee, classe, tf.inscription(), montantInscription(cycle));
            FraisScolaire scolarite =
                    fraisScolaire(annee, classe, tf.scolarite(), montantScolarite(cycle));
            fraisScolaire(annee, classe, tf.cantine(), 90_000d);
            fraisScolaire(annee, classe, tf.tenue(), 12_000d);
            fraisScolaire(annee, classe, tf.assurance(), 3_000d);
            fraisScolaire(annee, classe, tf.fournitures(), montantFournitures(cycle));
            if (classeExamen) {
                fraisScolaire(annee, classe, tf.examen(), 15_000d);
            }

            if (scolarite != null) {
                seedEcheancierScolarite(scolarite, annee);
            }
        }
    }

    private FraisScolaire fraisScolaire(AnneeScolaire annee, Classe classe, TypeFrais type,
                                        double montant) {
        // Colonne code = varchar(20) : on garde un code compact et stable.
        // Ex : FS-10-SCOL  (classeId - abréviation du type)
        String code = "FS-" + classe.getId() + "-" + abrege(type.getCode());
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

    /** Abréviation 4 lettres d'un code de type de frais (INSCRIPTION -> INSC). */
    private String abrege(String code) {
        if (code == null) return "XXXX";
        String c = code.toUpperCase();
        return c.length() <= 4 ? c : c.substring(0, 4);
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
        // Colonne code = varchar(20) : ex. ECH-123-T2
        e.setCode("ECH-" + frais.getId() + "-T" + numero);
        e.setFraisScolaireId(frais.getId());
        e.setNumero(numero);
        e.setLibelle(libelle);
        e.setDateEcheance(date);
        e.setMontant(montant);
        echeancierRepository.save(e);
    }

    // ─────────────────────────────────────────────────────────────
    //  Élèves + dossiers de démonstration
    // ─────────────────────────────────────────────────────────────
    private static final String[] NOMS = {
        "AGBODJAN", "HOUNKPATIN", "DOSSOU", "KPOGNON", "ADJOVI", "TOSSOU",
        "GBAGUIDI", "AHOYO", "ZINSOU", "DEGBO", "AMOUSSOU", "LOKO"
    };
    private static final String[] PRENOMS_G = {
        "Kévin", "Ange", "Rodrigue", "Yannick", "Parfait", "Serge", "Boris", "Éric"
    };
    private static final String[] PRENOMS_F = {
        "Grâce", "Chimène", "Reine", "Nadège", "Sonia", "Ornella", "Carine", "Laure"
    };
    /** Classes recevant des élèves de démo (primaire + collège + Terminale). */
    private static final String[] CLASSES_DEMO = {
        "CP-A", "CE1-A", "CE2-A", "CM1-A", "CM2-A",
        "6EME-A", "5EME-A", "4EME-A", "3EME-A", "TLE-A"
    };
    private static final int ELEVES_PAR_CLASSE = 2;

    private void seedElevesEtDossiers(AnneeScolaire annee) {
        Long statutInscritId = statutInscriptionRepository.findByCode("INSCRIT")
                .map(StatutInscription::getId).orElse(null);
        Long etapeValideeId = etapeRepository.findByCode("VALIDEE")
                .map(Etape::getId).orElse(null);
        if (statutInscritId == null || etapeValideeId == null) {
            log.warn("[demo-data] Statut INSCRIT ou étape VALIDEE absent — dossiers de démo ignorés.");
            return;
        }

        int seq = 0;
        int tuteurSeq = 0;
        Tuteur tuteurCourant = null;

        for (String classeCode : CLASSES_DEMO) {
            Classe classe = (Classe) classeRepository.findByCode(classeCode).orElse(null);
            if (classe == null) continue;
            String cycle = cycleDe(classeCode);

            for (int i = 1; i <= ELEVES_PAR_CLASSE; i++) {
                seq++;
                String eleveCode = "EL-" + classeCode + "-" + i;
                if (eleveRepository.findByCode(eleveCode).isPresent()) continue;

                // Un tuteur pour deux élèves
                if (tuteurCourant == null || seq % 2 == 1) {
                    tuteurSeq++;
                    tuteurCourant = tuteurDemo(tuteurSeq);
                }

                boolean garcon = seq % 2 == 0;
                String nom = NOMS[seq % NOMS.length];
                String prenom = garcon
                        ? PRENOMS_G[seq % PRENOMS_G.length]
                        : PRENOMS_F[seq % PRENOMS_F.length];
                LocalDate naissance = LocalDate.of(anneeNaissance(cycle), 1 + (seq % 12), 1 + (seq % 27));

                Eleve eleve = new Eleve();
                eleve.setCode(eleveCode);
                eleve.setNom(nom);
                eleve.setPrenom(prenom);
                eleve.setSexe(garcon ? "M" : "F");
                eleve.setDateNaissance(naissance);
                eleve.setClasseId(classe.getId());
                eleve.setTuteurId(tuteurCourant.getId());
                eleve.setActif(true);
                eleve = eleveRepository.save(eleve);

                DossierEleve d = new DossierEleve();
                d.setCode("DOS-" + classeCode + "-" + i);
                d.setEleveId(eleve.getId());
                d.setNom(nom);
                d.setPrenom(prenom);
                d.setSexe(eleve.getSexe());
                d.setDateNaissance(naissance);
                d.setTuteurId(tuteurCourant.getId());
                d.setClasseId(classe.getId());
                d.setAnneeScolaireId(annee.getId());
                d.setDateDebut(annee.getDateDebut());
                d.setStatutId(statutInscritId);
                d.setEtapeCouranteId(etapeValideeId);
                d.setRemise(0d);
                d.setNumero("2627-" + String.format("%03d", seq));
                dossierEleveRepository.save(d);
            }
        }
        log.info("[demo-data] {} élèves + dossiers de démo créés.", seq);
    }

    private Tuteur tuteurDemo(int n) {
        String code = "TUT-DEMO-" + n;
        return (Tuteur) tuteurRepository.findByCode(code).orElseGet(() -> {
            Tuteur t = new Tuteur();
            t.setCode(code);
            t.setNom(NOMS[n % NOMS.length]);
            t.setPrenom("Parent " + n);
            t.setTelephone1("0197" + String.format("%06d", 100000 + n));
            t.setEmail("parent.demo" + n + "@example.com");
            t.setActif(true);
            return tuteurRepository.save(t);
        });
    }

    private int anneeNaissance(String cycle) {
        return switch (cycle) {
            case "MATERNELLE" -> 2021;
            case "PRIMAIRE" -> 2017;
            case "COLLEGE" -> 2012;
            default -> 2008; // LYCEE
        };
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
