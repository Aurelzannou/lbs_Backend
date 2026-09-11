package com.App.lbs_backend.dto.response;

/** Une ligne de la vue globale « Tous les impayés » (`/comptabilite/suivi`, onglet global). */
public record SuiviGlobalLigneResponse(
        Long dossierEleveId,
        String eleveNomComplet,
        String classeLibelle,
        String anneeScolaireLibelle,
        Double totalDu,
        Double totalPaye,
        Double totalReste,
        /** EN_ATTENTE (rien payé) / PARTIELLE (payé en partie) / EN_RETARD (une échéance dépassée). */
        String statut
) {}
