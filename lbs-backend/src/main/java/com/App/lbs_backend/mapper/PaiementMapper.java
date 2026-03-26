package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.PaiementResponse;
import com.App.lbs_backend.entity.Paiement;
import org.springframework.stereotype.Component;

@Component
public class PaiementMapper implements Mapper<Paiement, PaiementResponse> {

    private final InscriptionMapper inscriptionMapper;
    private final FraisScolaireMapper fraisScolaireMapper;
    private final ModePaiementMapper modePaiementMapper;
    private final CaisseMapper caisseMapper;
    private final UtilisateurMapper utilisateurMapper;

    public PaiementMapper(InscriptionMapper inscriptionMapper,
                          FraisScolaireMapper fraisScolaireMapper,
                          ModePaiementMapper modePaiementMapper,
                          CaisseMapper caisseMapper,
                          UtilisateurMapper utilisateurMapper) {
        this.inscriptionMapper = inscriptionMapper;
        this.fraisScolaireMapper = fraisScolaireMapper;
        this.modePaiementMapper = modePaiementMapper;
        this.caisseMapper = caisseMapper;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    public PaiementResponse toResponse(Paiement entity) {
        if (entity == null) return null;
        return new PaiementResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getReference(),
                entity.getInscriptionId(),
                entity.getFraisScolaireId(),
                entity.getDatePaiement(),
                entity.getMontant(),
                entity.getModePaiementId(),
                entity.getCaisseId(),
                entity.getUtilisateurId(),
                entity.getObservation(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                inscriptionMapper.toResponse(entity.getInscription()),
                fraisScolaireMapper.toResponse(entity.getFraisScolaire()),
                modePaiementMapper.toResponse(entity.getModePaiement()),
                caisseMapper.toResponse(entity.getCaisse()),
                utilisateurMapper.toResponse(entity.getUtilisateur())
        );
    }
}