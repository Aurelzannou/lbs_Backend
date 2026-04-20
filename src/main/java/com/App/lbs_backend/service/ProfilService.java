package com.App.lbs_backend.service;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.request.ProfilRequest;
import com.App.lbs_backend.dto.response.ProfilResponse;
import com.App.lbs_backend.entity.Profil;
import com.App.lbs_backend.mapper.ProfilMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ProfilRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ProfilService extends AbstractBaseService<Profil, ProfilResponse> {

    private final ProfilRepository profilRepository;
    private final ProfilMapper profilMapper;
    private final KeycloakAdminService keycloakAdminService;

    public ProfilService(ProfilRepository profilRepository, ProfilMapper profilMapper, KeycloakAdminService keycloakAdminService) {
        super(Profil.class);
        this.profilRepository = profilRepository;
        this.profilMapper = profilMapper;
        this.keycloakAdminService = keycloakAdminService;
    }

    @Override
    public BaseRepository<Profil> repository() {
        return profilRepository;
    }

    @Override
    public Mapper<Profil, ProfilResponse> mapper() {
        return profilMapper;
    }

    @Override
    @Transactional
    public Profil create(Profil entity) {
        // Redéfinition si besoin, mais ici on va utiliser une méthode spécifique pour le DTO
        return super.create(entity);
    }

    @Transactional
    public Profil create(ProfilRequest request) {
        log.info("Création d'un nouveau profil : {}", request.getCode());
        
        Profil profil = new Profil();
        profil.setCode(request.getCode().toUpperCase());
        profil.setLibelle(request.getLibelle());
        
        Profil saved = profilRepository.save(profil);
        
        // Synchroniser avec Keycloak (créer le rôle)
        keycloakAdminService.createRole(saved.getCode(), saved.getLibelle());
        
        return saved;
    }

    @Transactional
    public Profil update(Long id, ProfilRequest request) {
        Profil profil = findById(id);
        profil.setLibelle(request.getLibelle());
        // On ne change généralement pas le code car il est lié aux rôles Keycloak
        return profilRepository.save(profil);
    }

    @Transactional
    public boolean delete(Long id) {
        Profil profil = findById(id);
        profilRepository.delete(profil);
        // Note: On pourrait aussi supprimer le rôle dans Keycloak ici si nécessaire
        return true;
    }
}
