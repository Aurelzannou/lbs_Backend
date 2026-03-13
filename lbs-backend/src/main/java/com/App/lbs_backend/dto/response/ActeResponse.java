package com.App.lbs_backend.dto.response;

import java.time.LocalDateTime;

public record ActeResponse(
        Integer id,
        String uuid,
        String code,
        String reference,
        Integer typeActeId,
        String cheminFichier,
        String nomFichier,
        Integer fichierId,
        LocalDateTime modifierLe,
        String modifierPar,
        TypeActeResponse typeActe,
        FichierResponse fichier
) {}