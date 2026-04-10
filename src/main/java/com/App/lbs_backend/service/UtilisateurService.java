package com.App.lbs_backend.service;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.UtilisateurResponse;
import com.App.lbs_backend.entity.Utilisateur;
import com.App.lbs_backend.mapper.UtilisateurMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService extends AbstractBaseService<Utilisateur, UtilisateurResponse> {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, UtilisateurMapper utilisateurMapper) {
        super(Utilisateur.class);
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
    }

    @Override
    protected BaseRepository<Utilisateur> repository() {
        return utilisateurRepository;
    }

    @Override
    protected Mapper<Utilisateur, UtilisateurResponse> mapper() {
        return utilisateurMapper;
    }
}
