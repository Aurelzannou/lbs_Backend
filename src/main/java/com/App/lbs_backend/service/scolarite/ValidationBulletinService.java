package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.dto.response.ValidationBulletinResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.ValidationBulletin;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.ValidationBulletinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidationBulletinService {

    private final ValidationBulletinRepository validationBulletinRepository;
    private final ClasseRepository classeRepository;

    /** Statut de validation de chaque classe pour une période — alimente l'écran de validation. */
    public List<ValidationBulletinResponse> listerParPeriode(Long periodeId, Long anneeScolaireId) {
        Map<Long, ValidationBulletin> parClasse = validationBulletinRepository
                .findByPeriodeIdAndAnneeScolaireId(periodeId, anneeScolaireId).stream()
                .collect(Collectors.toMap(ValidationBulletin::getClasseId, v -> v));

        List<Classe> classes = classeRepository.findAll();

        return classes.stream().map(classe -> {
            ValidationBulletinResponse dto = new ValidationBulletinResponse();
            dto.setClasseId(classe.getId());
            dto.setClasseLibelle(classe.getLibelle());
            dto.setPeriodeId(periodeId);
            dto.setAnneeScolaireId(anneeScolaireId);

            ValidationBulletin existant = parClasse.get(classe.getId());
            if (existant != null) {
                dto.setValide(Boolean.TRUE.equals(existant.getValide()));
                dto.setDateValidation(existant.getDateValidation());
                dto.setValideParEmail(existant.getValideParEmail());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public ValidationBulletinResponse getStatut(Long classeId, Long periodeId) {
        ValidationBulletin existant = validationBulletinRepository
                .findByClasseIdAndPeriodeId(classeId, periodeId).orElse(null);

        ValidationBulletinResponse dto = new ValidationBulletinResponse();
        dto.setClasseId(classeId);
        dto.setPeriodeId(periodeId);
        if (existant != null) {
            dto.setAnneeScolaireId(existant.getAnneeScolaireId());
            dto.setValide(Boolean.TRUE.equals(existant.getValide()));
            dto.setDateValidation(existant.getDateValidation());
            dto.setValideParEmail(existant.getValideParEmail());
        }
        return dto;
    }

    public ValidationBulletinResponse valider(Long classeId, Long periodeId, Long anneeScolaireId, String email) {
        ValidationBulletin vb = validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId)
                .orElseGet(ValidationBulletin::new);
        vb.setClasseId(classeId);
        vb.setPeriodeId(periodeId);
        vb.setAnneeScolaireId(anneeScolaireId);
        vb.setValide(true);
        vb.setDateValidation(LocalDateTime.now());
        vb.setValideParEmail(email);
        if (vb.getCode() == null) {
            vb.setCode("VAB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        }
        validationBulletinRepository.save(vb);
        return getStatut(classeId, periodeId);
    }

    public ValidationBulletinResponse devalider(Long classeId, Long periodeId) {
        validationBulletinRepository.findByClasseIdAndPeriodeId(classeId, periodeId).ifPresent(vb -> {
            vb.setValide(false);
            validationBulletinRepository.save(vb);
        });
        return getStatut(classeId, periodeId);
    }
}
