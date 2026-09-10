package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.request.EleveTuteurRequest;
import com.App.lbs_backend.dto.response.EleveTuteurResponse;
import com.App.lbs_backend.entity.EleveTuteur;
import com.App.lbs_backend.mapper.EleveTuteurMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.EleveTuteurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EleveTuteurService extends AbstractBaseService<EleveTuteur, EleveTuteurResponse> {

    private final EleveTuteurRepository eleveTuteurRepository;
    private final EleveTuteurMapper eleveTuteurMapper;

    public EleveTuteurService(EleveTuteurRepository eleveTuteurRepository, EleveTuteurMapper eleveTuteurMapper) {
        super(EleveTuteur.class);
        this.eleveTuteurRepository = eleveTuteurRepository;
        this.eleveTuteurMapper = eleveTuteurMapper;
    }

    @Override
    public BaseRepository<EleveTuteur> repository() {
        return eleveTuteurRepository;
    }

    @Override
    public Mapper<EleveTuteur, EleveTuteurResponse> mapper() {
        return eleveTuteurMapper;
    }

    @Transactional
    public EleveTuteurResponse associer(EleveTuteurRequest form) {
        eleveTuteurRepository.findByEleveIdAndTuteurId(form.getEleveId(), form.getTuteurId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ce parent est déjà associé à cet élève.");
                });

        EleveTuteur lien = new EleveTuteur();
        lien.setEleveId(form.getEleveId());
        lien.setTuteurId(form.getTuteurId());
        lien.setLienParente(form.getLienParente());
        lien.setContactUrgence(Boolean.TRUE.equals(form.getContactUrgence()));
        lien.setCode("ELT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        EleveTuteur saved = create(lien);
        return toResponse(saved.getId());
    }

    public EleveTuteurResponse modifier(String uuid, EleveTuteurRequest form) {
        EleveTuteur lien = findByUuid(uuid);
        lien.setLienParente(form.getLienParente());
        lien.setContactUrgence(Boolean.TRUE.equals(form.getContactUrgence()));
        update(lien);
        return toResponse(lien.getId());
    }

    @Transactional(readOnly = true)
    public List<EleveTuteurResponse> listerParEleve(Long eleveId) {
        return eleveTuteurRepository.findByEleveId(eleveId).stream()
                .map(eleveTuteurMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EleveTuteurResponse> listerParTuteur(Long tuteurId) {
        return eleveTuteurRepository.findByTuteurId(tuteurId).stream()
                .map(eleveTuteurMapper::toResponse)
                .toList();
    }
}
