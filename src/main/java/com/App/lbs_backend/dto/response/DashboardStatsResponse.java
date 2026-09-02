package com.App.lbs_backend.dto.response;

import java.util.List;

/** Agrégats du tableau de bord administrateur. */
public record DashboardStatsResponse(
        String anneeScolaireLibelle,

        long nbEleves,
        long nbProfesseurs,
        long nbClasses,

        long dossiersDeposes,
        long dossiersAcceptes,
        long dossiersInscrits,
        long dossiersRefuses,
        long totalDossiers,

        double totalEncaisse,
        double totalDepenses,
        double soldeCaisses,
        double resteAPayerTotal,

        List<PointMensuel> inscriptionsParMois,
        List<PointMensuel> encaissementsParMois,
        List<Repartition> dossiersParStatut,
        List<Repartition> effectifParClasse,

        NotesBulletins notesBulletins
) {
    /** Un point d'une série mensuelle. mois au format "yyyy-MM". */
    public record PointMensuel(String mois, double valeur) {}

    /** Une part d'une répartition (camembert / barres). */
    public record Repartition(String libelle, long valeur) {}

    /** Avancement de la période académique en cours + moyennes des classes déjà validées. */
    public record NotesBulletins(
            String periodeLibelle,
            long feuillesSoumises,          // feuilles de notes soumises par les professeurs
            long feuillesAttendues,
            long classesBulletinValide,     // classes dont le bulletin est validé
            long classesTotal,
            Double moyenneEtablissement,    // sur les classes validées ; null si aucune
            Double tauxReussite,            // % d'élèves ≥ 10 sur les classes validées ; null si aucune
            List<Repartition> repartitionMoyennes,

            int nbPeriodesAnnee,
            long matieresValideesAnnee,     // total matières validées sur toute l'année
            long matieresAttenduesAnnee,
            List<AvancementClasse> validationParClasse,

            // À traiter, sur la période en cours
            long matieresAValider,          // soumises par les profs, en attente de validation admin
            long matieresEnSaisie,          // ont des notes mais pas encore soumises
            List<MatiereATraiter> matieresATraiter   // le détail : salle + matière + état
    ) {}

    /** Matières validées vs attendues (matières × périodes) pour une classe, sur l'année. */
    public record AvancementClasse(String classeLibelle, long matieresValidees, long matieresAttendues) {}

    /**
     * Une matière qui demande une action, sur la période en cours.
     * @param etat "A_VALIDER" (soumise par le prof) ou "EN_SAISIE" (notes présentes, pas soumise)
     * @param pretePourSoumission true si le prof a verrouillé toutes les colonnes sans soumettre
     *                            (état EN_SAISIE) — à relancer.
     */
    public record MatiereATraiter(String classeLibelle, String matiereLibelle,
                                  String etat, boolean pretePourSoumission) {}
}
