package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodeAcademiqueResponse(
        Long id,
        String uuid,
        String code,
        String libelle,
        Long anneeScolaireId,
        LocalDate dateDebut,
        LocalDate dateFin,
        Boolean verrouille,
        LocalDateTime modifierLe,
        String modifierPar,
        AnneeScolaireResponse anneeScolaire
) {}
