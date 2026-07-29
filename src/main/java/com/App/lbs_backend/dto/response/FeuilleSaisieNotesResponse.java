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
    private List<EleveNoteDto> eleves;

    @Data
    public static class EleveNoteDto {
        private Long eleveId;
        private String nom;
        private String prenom;
        private Double interrogation;
        private Double devoir1;
        private Double devoir2;
        /** Moyenne des valeurs non nulles parmi {interrogation, devoir1, devoir2} — calculée côté
            serveur pour affichage initial, le client la recalcule en direct à la saisie. */
        private Double moyenne;
    }
}
