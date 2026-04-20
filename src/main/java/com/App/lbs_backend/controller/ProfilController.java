package com.App.lbs_backend.controller;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.ProfilRequest;
import com.App.lbs_backend.dto.response.ProfilResponse;
import com.App.lbs_backend.entity.Profil;
import com.App.lbs_backend.service.ProfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/administration/profils")
@RequiredArgsConstructor
public class ProfilController extends MasterController<Profil, ProfilResponse, ProfilRequest> {

    private final ProfilService profilService;

    @Override
    protected AbstractBaseService<Profil, ProfilResponse> service() {
        return profilService;
    }

    @Override
    protected ProfilResponse doCreate(ProfilRequest form) {
        return profilService.mapper().toResponse(profilService.create(form));
    }

    @Override
    protected ProfilResponse doUpdate(String uuid, ProfilRequest form) {
        Profil profil = profilService.findByUuid(uuid);
        return profilService.mapper().toResponse(profilService.update(profil.getId(), form));
    }
}
