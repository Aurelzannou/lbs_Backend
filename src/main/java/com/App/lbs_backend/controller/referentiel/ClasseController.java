package com.App.lbs_backend.controller.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.core.MasterController;
import com.App.lbs_backend.dto.request.ClasseRequest;
import com.App.lbs_backend.dto.request.ClasseRequest.MatiereCoefficientRequest;
import com.App.lbs_backend.dto.response.ClasseResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.service.referentiel.ClasseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClasseController extends MasterController<Classe, ClasseResponse, ClasseRequest> {

    private final ClasseService classeService;

    @Override
    protected AbstractBaseService<Classe, ClasseResponse> service() {
        return classeService;
    }

    @Override
    protected ClasseResponse doCreate(ClasseRequest form) {
        Classe entity = new Classe();
        appliquer(entity, form);

        Classe saved = classeService.create(entity);
        classeService.synchroniserCoefficients(form.getNiveauId(), coefficientsDepuisForm(form));
        return classeService.toResponse(saved.getId());
    }

    @Override
    protected ClasseResponse doUpdate(String uuid, ClasseRequest form) {
        Classe entity = classeService.findByUuid(uuid);
        appliquer(entity, form);

        classeService.update(entity);
        classeService.synchroniserCoefficients(form.getNiveauId(), coefficientsDepuisForm(form));
        return classeService.toResponse(entity.getId());
    }

    private void appliquer(Classe entity, ClasseRequest form) {
        entity.setCode(form.getCode());
        entity.setLibelle(form.getLibelle());
        entity.setNiveauId(form.getNiveauId());
        entity.setProfId(form.getProfId());
        entity.setCapaciteMax(form.getCapaciteMax());
        entity.setActif(form.getActif());
        entity.setMatiereIds(matiereIdsDepuisForm(form));
    }

    /** Liste des matières de la classe : priorité au nouveau format {matière, coefficient}.
        Toujours renvoyer une liste MUTABLE — Hibernate remplace le contenu de la collection
        {@code @ElementCollection} en place lors du flush (une liste immuable lève
        UnsupportedOperationException au moment de l'enregistrement). */
    private List<Long> matiereIdsDepuisForm(ClasseRequest form) {
        if (form.getMatieres() != null && !form.getMatieres().isEmpty()) {
            return form.getMatieres().stream()
                    .map(MatiereCoefficientRequest::getMatiereId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return form.getMatiereIds() != null ? new ArrayList<>(form.getMatiereIds()) : new ArrayList<>();
    }

    private Map<Long, Double> coefficientsDepuisForm(ClasseRequest form) {
        if (form.getMatieres() == null) return Map.of();
        return form.getMatieres().stream()
                .filter(m -> m.getMatiereId() != null)
                .collect(Collectors.toMap(
                        MatiereCoefficientRequest::getMatiereId,
                        m -> m.getCoefficient() != null && m.getCoefficient() > 0 ? m.getCoefficient() : 1.0,
                        (a, b) -> b));
    }
}
