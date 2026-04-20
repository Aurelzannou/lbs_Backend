package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.TypeActeResponse;
import com.App.lbs_backend.entity.TypeActe;
import com.App.lbs_backend.mapper.TypeActeMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.TypeActeRepository;
import org.springframework.stereotype.Service;

@Service
public class TypeActeService extends AbstractBaseService<TypeActe, TypeActeResponse> {
    
    private final TypeActeRepository typeActeRepository;
    private final TypeActeMapper typeActeMapper;

    public TypeActeService(TypeActeRepository typeActeRepository, TypeActeMapper typeActeMapper) {
        super(TypeActe.class);
        this.typeActeRepository = typeActeRepository;
        this.typeActeMapper = typeActeMapper;
    }

    @Override
    public BaseRepository<TypeActe> repository() {
        return typeActeRepository;
    }

    @Override
    public Mapper<TypeActe, TypeActeResponse> mapper() {
        return typeActeMapper;
    }
}
