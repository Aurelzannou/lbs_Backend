package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ActeResponse(
        Long id,
        String uuid,
        String code,
        String reference,
        Long typeActeId,
        String cheminFichier,
        String nomFichier,
        Long fichierId,
        LocalDateTime modifierLe,
        String modifierPar,
        TypeActeResponse typeActe,
        FichierResponse fichier
) {}