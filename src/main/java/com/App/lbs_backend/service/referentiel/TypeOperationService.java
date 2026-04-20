package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.TypeOperationResponse;
import com.App.lbs_backend.entity.TypeOperation;
import com.App.lbs_backend.mapper.TypeOperationMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.TypeOperationRepository;
import org.springframework.stereotype.Service;

@Service
public class TypeOperationService extends AbstractBaseService<TypeOperation, TypeOperationResponse> {
    
    private final TypeOperationRepository typeOperationRepository;
    private final TypeOperationMapper typeOperationMapper;

    public TypeOperationService(TypeOperationRepository typeOperationRepository, TypeOperationMapper typeOperationMapper) {
        super(TypeOperation.class);
        this.typeOperationRepository = typeOperationRepository;
        this.typeOperationMapper = typeOperationMapper;
    }

    @Override
    public BaseRepository<TypeOperation> repository() {
        return typeOperationRepository;
    }

    @Override
    public Mapper<TypeOperation, TypeOperationResponse> mapper() {
        return typeOperationMapper;
    }
}
