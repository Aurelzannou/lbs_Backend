package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.http.response.MetaResponse;
import com.App.lbs_backend.core.http.response.PageResponse;
import com.App.lbs_backend.dto.response.TuteurResponse;
import com.App.lbs_backend.entity.Tuteur;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.mapper.TuteurMapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.TuteurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TuteurService extends AbstractBaseService<Tuteur, TuteurResponse> {

    private final TuteurRepository tuteurRepository;
    private final TuteurMapper tuteurMapper;

    public TuteurService(TuteurRepository tuteurRepository, TuteurMapper tuteurMapper) {
        super(Tuteur.class);
        this.tuteurRepository = tuteurRepository;
        this.tuteurMapper = tuteurMapper;
    }

    @Override
    public BaseRepository<Tuteur> repository() {
        return tuteurRepository;
    }

    @Override
    public Mapper<Tuteur, TuteurResponse> mapper() {
        return tuteurMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<TuteurResponse> rechercher(String filter, Pageable pageable) {
        Page<Tuteur> page = tuteurRepository.searchFiltered(filter, pageable);
        List<TuteurResponse> items = page.getContent().stream()
                .map(tuteurMapper::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(items, MetaResponse.ofPage(page));
    }
}
