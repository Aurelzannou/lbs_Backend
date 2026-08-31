package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.MouvementCaisseResponse;
import com.App.lbs_backend.entity.MouvementCaisse;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.MouvementCaisseMapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.MouvementCaisseRepository;
import org.springframework.stereotype.Service;

@Service
public class MouvementCaisseService extends AbstractBaseService<MouvementCaisse, MouvementCaisseResponse> {

    private final MouvementCaisseRepository mouvementCaisseRepository;
    private final MouvementCaisseMapper mouvementCaisseMapper;

    public MouvementCaisseService(MouvementCaisseRepository mouvementCaisseRepository, MouvementCaisseMapper mouvementCaisseMapper) {
        super(MouvementCaisse.class);
        this.mouvementCaisseRepository = mouvementCaisseRepository;
        this.mouvementCaisseMapper = mouvementCaisseMapper;
    }

    @Override
    public BaseRepository<MouvementCaisse> repository() {
        return mouvementCaisseRepository;
    }

    @Override
    public Mapper<MouvementCaisse, MouvementCaisseResponse> mapper() {
        return mouvementCaisseMapper;
    }

    public java.util.List<MouvementCaisseResponse> listerParCaisse(Long caisseId) {
        return mouvementCaisseRepository.findByCaisseIdOrderByDateMouvementDesc(caisseId).stream()
                .map(mouvementCaisseMapper::toResponse)
                .toList();
    }
}
