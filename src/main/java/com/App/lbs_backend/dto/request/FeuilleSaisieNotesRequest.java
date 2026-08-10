package com.App.lbs_backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class FeuilleSaisieNotesRequest {
    private Long classeId;
    private Long matiereId;
    private Long periodeId;
    /** Renseigné uniquement quand l'appel vient du portail professeur — sert à vérifier qu'il
        enseigne bien cette (classe, matière) et à attribuer les notes à son nom. */
    private Long professeurId;
    private List<EleveNoteEntry> eleves;

    @Data
    public static class EleveNoteEntry {
        private Long eleveId;
        /** Autant de valeurs que le professeur a saisi d'interrogations (position i = numéro i+1).
            Une entrée à null représente une interrogation non notée pour cet élève (ex: absent). */
        private List<Double> interrogations;
        private Double devoir1;
        private Double devoir2;
    }
}
