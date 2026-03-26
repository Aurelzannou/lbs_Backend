package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.InscriptionResponse;
import com.App.lbs_backend.entity.Inscription;
import org.springframework.stereotype.Component;

@Component
public class InscriptionMapper implements Mapper<Inscription, InscriptionResponse> {

    private final EleveMapper eleveMapper;
    private final ClasseMapper classeMapper;
    private final AnneeScolaireMapper anneeScolaireMapper;
    private final TypeOperationMapper typeOperationMapper;
    private final ActeMapper acteMapper;
    private final UtilisateurMapper utilisateurMapper;
    private final StatutInscriptionMapper statutInscriptionMapper;

    public InscriptionMapper(EleveMapper eleveMapper,
                             ClasseMapper classeMapper,
                             AnneeScolaireMapper anneeScolaireMapper,
                             TypeOperationMapper typeOperationMapper,
                             ActeMapper acteMapper,
                             UtilisateurMapper utilisateurMapper,
                             StatutInscriptionMapper statutInscriptionMapper) {
        this.eleveMapper = eleveMapper;
        this.classeMapper = classeMapper;
        this.anneeScolaireMapper = anneeScolaireMapper;
        this.typeOperationMapper = typeOperationMapper;
        this.acteMapper = acteMapper;
        this.utilisateurMapper = utilisateurMapper;
        this.statutInscriptionMapper = statutInscriptionMapper;
    }

    @Override
    public InscriptionResponse toResponse(Inscription entity) {
        if (entity == null) return null;
        return new InscriptionResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getEleveId(),
                entity.getClasseId(),
                entity.getAnneeScolaireId(),
                entity.getDateDebut(),
                entity.getDateFin(),
                entity.getStatutId(),
                entity.getRemise(),
                entity.getNumero(),
                entity.getTypeOperationId(),
                entity.getActeId(),
                entity.getUtilisateurId(),
                entity.getModifierLe(),
                entity.getModifierPar(),
                eleveMapper.toResponse(entity.getEleve()),
                classeMapper.toResponse(entity.getClasse()),
                anneeScolaireMapper.toResponse(entity.getAnneeScolaire()),
                typeOperationMapper.toResponse(entity.getTypeOperation()),
                acteMapper.toResponse(entity.getActe()),
                utilisateurMapper.toResponse(entity.getUtilisateur()),
                statutInscriptionMapper.toResponse(entity.getStatut())
        );
    }
}
