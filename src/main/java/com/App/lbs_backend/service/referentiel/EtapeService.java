package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.exception.EntityNotFoundException;
import com.App.lbs_backend.dto.request.EtapeRequest;
import com.App.lbs_backend.dto.response.EtapeResponse;
import com.App.lbs_backend.entity.Etape;
import com.App.lbs_backend.mapper.EtapeMapper;
import com.App.lbs_backend.repository.EtapeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtapeService {

    private final EtapeRepository etapeRepository;
    private final EtapeMapper etapeMapper;

    public List<EtapeResponse> getAll() {
        return etapeRepository.findAll().stream()
                .map(etapeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public EtapeResponse getByUuid(String uuid) {
        return etapeRepository.findAll().stream()
                .filter(e -> uuid.equals(e.getUuid()))
                .findFirst()
                .map(etapeMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Etape", uuid));
    }

    @Transactional
    public EtapeResponse create(EtapeRequest request) {
        Etape entity = etapeMapper.toEntity(request);
        entity = etapeRepository.save(entity);
        return etapeMapper.toResponse(entity);
    }

    @Transactional
    public EtapeResponse update(String uuid, EtapeRequest request) {
        Etape entity = etapeRepository.findAll().stream()
                .filter(e -> uuid.equals(e.getUuid()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Etape", uuid));
                
        etapeMapper.updateEntityFromRequest(request, entity);
        entity = etapeRepository.save(entity);
        return etapeMapper.toResponse(entity);
    }

    @Transactional
    public void delete(String uuid) {
        Etape entity = etapeRepository.findAll().stream()
                .filter(e -> uuid.equals(e.getUuid()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Etape", uuid));
        etapeRepository.delete(entity);
    }
}
