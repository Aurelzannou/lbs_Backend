package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.ClasseResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.mapper.ClasseMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ClasseRepository;
import org.springframework.stereotype.Service;

@Service
public class ClasseService extends AbstractBaseService<Classe, ClasseResponse> {
    
    private final ClasseRepository classeRepository;
    private final ClasseMapper classeMapper;

    public ClasseService(ClasseRepository classeRepository, ClasseMapper classeMapper) {
        super(Classe.class);
        this.classeRepository = classeRepository;
        this.classeMapper = classeMapper;
    }

    @Override
    public BaseRepository<Classe> repository() {
        return classeRepository;
    }

    @Override
    public Mapper<Classe, ClasseResponse> mapper() {
        return classeMapper;
    }
}
