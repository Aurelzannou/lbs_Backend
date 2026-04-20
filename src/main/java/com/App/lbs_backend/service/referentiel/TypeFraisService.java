package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.TypeFraisResponse;
import com.App.lbs_backend.entity.TypeFrais;
import com.App.lbs_backend.mapper.TypeFraisMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.TypeFraisRepository;
import org.springframework.stereotype.Service;

@Service
public class TypeFraisService extends AbstractBaseService<TypeFrais, TypeFraisResponse> {
    
    private final TypeFraisRepository typeFraisRepository;
    private final TypeFraisMapper typeFraisMapper;

    public TypeFraisService(TypeFraisRepository typeFraisRepository, TypeFraisMapper typeFraisMapper) {
        super(TypeFrais.class);
        this.typeFraisRepository = typeFraisRepository;
        this.typeFraisMapper = typeFraisMapper;
    }

    @Override
    public BaseRepository<TypeFrais> repository() {
        return typeFraisRepository;
    }

    @Override
    public Mapper<TypeFrais, TypeFraisResponse> mapper() {
        return typeFraisMapper;
    }
}
