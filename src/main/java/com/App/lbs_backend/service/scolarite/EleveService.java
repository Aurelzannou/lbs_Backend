package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.http.response.MetaResponse;
import com.App.lbs_backend.core.http.response.PageResponse;
import com.App.lbs_backend.dto.response.EleveResponse;
import com.App.lbs_backend.entity.Eleve;
import com.App.lbs_backend.mapper.EleveMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.EleveRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EleveService extends AbstractBaseService<Eleve, EleveResponse> {

    private final EleveRepository eleveRepository;
    private final EleveMapper eleveMapper;

    public EleveService(EleveRepository eleveRepository, EleveMapper eleveMapper) {
        super(Eleve.class);
        this.eleveRepository = eleveRepository;
        this.eleveMapper = eleveMapper;
    }

    @Override
    public BaseRepository<Eleve> repository() {
        return eleveRepository;
    }

    @Override
    public Mapper<Eleve, EleveResponse> mapper() {
        return eleveMapper;
    }

    /** Recherche paginée avec filtre texte + classe. */
    public Page<Eleve> searchFiltered(Long classeId, String filter, Pageable pageable) {
        return eleveRepository.searchFiltered(classeId, filter, pageable);
    }

    /** Convertit une Page JPA en PageResponse DTO. */
    public PageResponse<EleveResponse> toPageResponse(Page<Eleve> page) {
        List<EleveResponse> items = page.getContent().stream()
                .map(e -> mapper().toResponse(e))
                .collect(Collectors.toList());
        return new PageResponse<>(items, MetaResponse.ofPage(page));
    }
}
