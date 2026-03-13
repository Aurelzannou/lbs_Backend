package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.DepenseScolaireResponse;
import com.App.lbs_backend.entity.DepenseScolaire;
import org.springframework.stereotype.Component;

@Component
public class DepenseScolaireMapper implements Mapper<DepenseScolaire, DepenseScolaireResponse> {

    private final CaisseMapper caisseMapper;
    private final CategorieDepenseMapper categorieDepenseMapper;
    private final UtilisateurMapper utilisateurMapper;

    public DepenseScolaireMapper(CaisseMapper caisseMapper,
                                 CategorieDepenseMapper categorieDepenseMapper,
                                 UtilisateurMapper utilisateurMapper) {
        this.caisseMapper = caisseMapper;
        this.categorieDepenseMapper = categorieDepenseMapper;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    public DepenseScolaireResponse toResponse(DepenseScolaire entity) {
        if (entity == null) return null;
        return new DepenseScolaireResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getReference(),
                entity.getCaisseId(),
                entity.getCategorieDepenseId(),
                entity.getMontant(),
                entity.getDateDepense(),
                entity.getMotif(),
                entity.getUtilisateurId(),
                entity.getModifierLe(),
                caisseMapper.toResponse(entity.getCaisse()),
                categorieDepenseMapper.toResponse(entity.getCategorieDepense()),
                utilisateurMapper.toResponse(entity.getUtilisateur())
        );
    }
}