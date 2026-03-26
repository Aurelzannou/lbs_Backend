package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InscriptionResponse(
        Integer id,
        String uuid,
        String code,
        Integer eleveId,
        Integer classeId,
        Integer anneeScolaireId,
        LocalDate dateDebut,
        LocalDate dateFin,
        Integer statutId,
        Double remise,
        String numero,
        Integer typeOperationId,
        Integer acteId,
        Integer utilisateurId,
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
