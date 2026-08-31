package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.MouvementCaisseResponse;
import com.App.lbs_backend.entity.MouvementCaisse;
import org.springframework.stereotype.Component;

@Component
public class MouvementCaisseMapper implements Mapper<MouvementCaisse, MouvementCaisseResponse> {

    @Override
    public MouvementCaisseResponse toResponse(MouvementCaisse entity) {
        if (entity == null) return null;
        return new MouvementCaisseResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getCode(),
                entity.getCaisseId(),
                entity.getCaisse() != null ? entity.getCaisse().getLibelle() : null,
                entity.getTypeMouvement(),
                entity.getMontant(),
                entity.getDateMouvement(),
                entity.getSource(),
                entity.getSourceId(),
                entity.getSoldeApres(),
                entity.getDescription()
        );
    }
}
