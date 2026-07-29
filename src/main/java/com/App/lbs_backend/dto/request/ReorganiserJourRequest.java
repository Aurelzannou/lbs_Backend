package com.App.lbs_backend.dto.request;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * Réorganisation atomique de l'emploi du temps d'un jour donné (glisser-déposer) :
 * on envoie le nouvel horaire de chaque cours concerné en une seule fois, pour que le
 * serveur valide et applique l'ensemble sans faux conflit transitoire entre les cours
 * du même lot.
 */
@Data
public class ReorganiserJourRequest {
    private String jour;
    private List<Creneau> seances;

    @Data
    public static class Creneau {
        private String uuid;
        private LocalTime heureDebut;
        private LocalTime heureFin;
    }
}
