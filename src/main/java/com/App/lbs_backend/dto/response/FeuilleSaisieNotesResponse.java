package com.App.lbs_backend.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class FeuilleSaisieNotesResponse {
    private Long classeId;
    private String classeLibelle;
    private Long matiereId;
    private String matiereLibelle;
    private Long periodeId;
    private String periodeLibelle;
    /** true si le bulletin de cette classe/période est déjà validé — la saisie est alors bloquée. */
    private boolean valide;
    /** true si un professeur est assigné à cette classe + matière (sinon la saisie relève de
        l'administration, ex. la conduite). Sert à l'écran admin : tant que le professeur n'a pas
        envoyé la matière, l'admin ne doit pas la modifier. */
    private boolean professeurAssigne;
    /** Nombre de colonnes "Interrogation" à afficher (le professeur peut en ajouter d'autres). */
    private int nombreInterrogations;
    /** true pour la matière "Conduite" — sa moyenne n'est jamais divisée par 3 (une seule valeur
        suffit) et le champ Interrogation 1 peut être pré-rempli d'une suggestion automatique. */
    private boolean estConduite;
    private List<EleveNoteDto> eleves;

    @Data
    public static class EleveNoteDto {
        private Long eleveId;
        private String nom;
        private String prenom;
        /** Taille alignée sur nombreInterrogations. */
        private List<Double> interrogations;
        private Double devoir1;
        private Double devoir2;
        /** Moyenne des interrogations non nulles = Somme / Nombre. */
        private Double moyenneInterrogations;
        /** Moyenne des valeurs non nulles parmi {moyenneInterrogations, devoir1, devoir2} —
            calculée côté serveur pour affichage initial, le client la recalcule en direct. */
        private Double moyenne;
    }
}
