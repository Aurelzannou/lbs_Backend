package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InscriptionResponse(
        Long id,
        String uuid,
        String code,
        Long eleveId,
        Long classeId,
        Long anneeScolaireId,
        LocalDate dateDebut,
        LocalDate dateFin,
        Long statutId,
        Double remise,
        String numero,
        Long typeOperationId,
        Long acteId,
        Long utilisateurId,
        LocalDateTime modifierLe,
        String modifierPar,
        EleveResponse eleve,
        ClasseResponse classe,
        AnneeScolaireResponse anneeScolaire,
        TypeOperationResponse typeOperation,
        ActeResponse acte,
        UtilisateurResponse utilisateur,
        StatutInscriptionResponse statut
) {}
