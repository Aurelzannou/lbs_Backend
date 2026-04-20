package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.CategorieDepenseResponse;
import com.App.lbs_backend.entity.CategorieDepense;
import com.App.lbs_backend.mapper.CategorieDepenseMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.CategorieDepenseRepository;
import org.springframework.stereotype.Service;

@Service
public class CategorieDepenseService extends AbstractBaseService<CategorieDepense, CategorieDepenseResponse> {
    
    private final CategorieDepenseRepository categorieDepenseRepository;
    private final CategorieDepenseMapper categorieDepenseMapper;

    public CategorieDepenseService(CategorieDepenseRepository categorieDepenseRepository, CategorieDepenseMapper categorieDepenseMapper) {
        super(CategorieDepense.class);
        this.categorieDepenseRepository = categorieDepenseRepository;
        this.categorieDepenseMapper = categorieDepenseMapper;
    }

    @Override
    public BaseRepository<CategorieDepense> repository() {
        return categorieDepenseRepository;
    }

    @Override
    public Mapper<CategorieDepense, CategorieDepenseResponse> mapper() {
        return categorieDepenseMapper;
    }
}
