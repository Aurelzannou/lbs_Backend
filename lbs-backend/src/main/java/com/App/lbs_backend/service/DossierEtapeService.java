package com.App.lbs_backend.service;

import com.App.lbs_backend.core.exception.EntityNotFoundException;
import com.App.lbs_backend.dto.request.DossierEtapeRequest;
import com.App.lbs_backend.dto.response.DossierEtapeResponse;
import com.App.lbs_backend.entity.DossierEtape;
import com.App.lbs_backend.mapper.DossierEtapeMapper;
import com.App.lbs_backend.repository.DossierEtapeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DossierEtapeService {

    private final DossierEtapeRepository dossierEtapeRepository;
    private final DossierEtapeMapper dossierEtapeMapper;

    public List<DossierEtapeResponse> getAll() {
        return dossierEtapeRepository.findAll().stream()
                .map(dossierEtapeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<DossierEtapeResponse> getByDossierEleveId(Integer dossierEleveId) {
        return dossierEtapeRepository.findByDossierEleveId(dossierEleveId).stream()
                .map(dossierEtapeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public DossierEtapeResponse getByUuid(String uuid) {
        return dossierEtapeRepository.findAll().stream()
                .filter(e -> uuid.equals(e.getUuid()))
                .findFirst()
                .map(dossierEtapeMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("DossierEtape", uuid));
    }

    @Transactional
    public DossierEtapeResponse create(DossierEtapeRequest request) {
        DossierEtape entity = dossierEtapeMapper.toEntity(request);
        entity = dossierEtapeRepository.save(entity);
        return dossierEtapeMapper.toResponse(entity);
    }

    @Transactional
    public DossierEtapeResponse update(String uuid, DossierEtapeRequest request) {
        DossierEtape entity = dossierEtapeRepository.findAll().stream()
                .filter(e -> uuid.equals(e.getUuid()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("DossierEtape", uuid));
                
        dossierEtapeMapper.updateEntityFromRequest(request, entity);
        entity = dossierEtapeRepository.save(entity);
        return dossierEtapeMapper.toResponse(entity);
    }

    @Transactional
    public void delete(String uuid) {
        DossierEtape entity = dossierEtapeRepository.findAll().stream()
                .filter(e -> uuid.equals(e.getUuid()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("DossierEtape", uuid));
        dossierEtapeRepository.delete(entity);
    }
}
