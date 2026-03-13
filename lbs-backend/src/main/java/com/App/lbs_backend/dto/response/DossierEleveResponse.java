package com.App.lbs_backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DossierEleveResponse(
        Integer id,
        String uuid,
        String code,
        String nomEleve,
        String prenomEleve,
        String sexe,
        Integer age,
        String matricule,
        Boolean actif,
        Integer classeId,
        LocalDate dateDebut,
        LocalDate dateFin,
        String soufant,
        String numero,
        Integer utilisateurId,
        String provenance,
        Integer typeOperationId,
        Integer acteId,
        String photo,
        Integer anneeScolaireId,
        Double remise,
        LocalDateTime modifierLe,
        String modifierPar,
        ClasseResponse classe,
        TypeOperationResponse typeOperation,
        ActeResponse acte,
        UtilisateurResponse utilisateur,
        AnneeScolaireResponse anneeScolaire
) {}