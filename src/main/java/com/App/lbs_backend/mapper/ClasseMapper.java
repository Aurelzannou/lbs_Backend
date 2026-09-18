package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.ClasseResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.Coefficient;
import com.App.lbs_backend.repository.CoefficientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ClasseMapper implements Mapper<Classe, ClasseResponse> {

    private final ProfesseurMapper professeurMapper;
    private final NiveauMapper niveauMapper;
    private final CoefficientRepository coefficientRepository;

    public ClasseMapper(ProfesseurMapper professeurMapper, NiveauMapper niveauMapper,
                        CoefficientRepository coefficientRepository) {
        this.professeurMapper = professeurMapper;
        this.niveauMapper = niveauMapper;
        this.coefficientRepository = coefficientRepository;
    }

    @Override
    public ClasseResponse toResponse(Classe entity) {
        if (entity == null) return null;

        Map<Long, Double> coefficients = entity.getNiveauId() == null ? Map.of()
                : coefficientRepository.findByNiveauId(entity.getNiveauId()).stream()
                    .filter(c -> c.getMatiereId() != null && c.getValeur() != null)
                    .collect(Collectors.toMap(Coefficient::getMatiereId, Coefficient::getValeur, (a, b) -> b));

        // entity.getMatiereIds() est une collection LAZY (@ElementCollection) : on la recopie ici,
        // dans la transaction du mapper, pour forcer son chargement. Sans ça, le DTO garde une
        // référence vers la collection Hibernate non initialisée, et Jackson plante à la
        // sérialisation une fois la session fermée ("no session").
        List<Long> matiereIds = entity.getMatiereIds() == null ? List.of() : List.copyOf(entity.getMatiereIds());

        return new ClasseResponse(
                entity.getId(),
                entity.getUuid(),
                entity.getProfId(),
                entity.getCode(),
                entity.getLibelle(),
                entity.getNiveauId(),
                entity.getCapaciteMax(),
                entity.getActif(),
                matiereIds,
                coefficients,
                entity.getModifierLe(),
                entity.getModifierPar(),
                professeurMapper.toResponse(entity.getProfesseur()),
                niveauMapper.toResponse(entity.getNiveau())
        );
    }
}
