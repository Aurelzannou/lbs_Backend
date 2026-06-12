package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodeRentreeResponse(
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
