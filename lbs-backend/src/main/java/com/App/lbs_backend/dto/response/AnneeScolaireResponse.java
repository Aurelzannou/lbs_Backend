package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnneeScolaireResponse(
        Integer id,
        String uuid,
        String code,
        String libelle,
        LocalDate dateDebut,
        LocalDate dateFin,
        Boolean actif,
        LocalDateTime modifierLe
) {}