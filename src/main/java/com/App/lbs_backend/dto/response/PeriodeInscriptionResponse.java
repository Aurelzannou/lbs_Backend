package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodeInscriptionResponse(
        Long id,
        String uuid,
        String libelle,
        Long anneeScolaireId,
        String anneeScolaireLibelle,
        LocalDate dateOuverture,
        LocalDate dateCloture,
        Boolean actif,
        String statut,
        LocalDateTime modifierLe
) {}
