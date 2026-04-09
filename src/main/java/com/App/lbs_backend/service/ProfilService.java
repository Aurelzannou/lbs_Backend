package com.App.lbs_backend.service;

import com.App.lbs_backend.dto.request.ProfilRequest;
import com.App.lbs_backend.entity.Profil;
import com.App.lbs_backend.repository.ProfilRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfilService {

    private final ProfilRepository profilRepository;
    private final KeycloakAdminService keycloakAdminService;

    public List<Profil> getAll() {
        return profilRepository.findAll();
    }

    public Profil getById(Long id) {
        return profilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profil non trouvé avec l'id : " + id));
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
        Profil profil = getById(id);
        profil.setLibelle(request.getLibelle());
        // On ne change généralement pas le code car il est lié aux rôles Keycloak
        return profilRepository.save(profil);
    }

    @Transactional
    public void delete(Long id) {
        Profil profil = getById(id);
        profilRepository.delete(profil);
        // Note: On pourrait aussi supprimer le rôle dans Keycloak ici si nécessaire
    }
}
