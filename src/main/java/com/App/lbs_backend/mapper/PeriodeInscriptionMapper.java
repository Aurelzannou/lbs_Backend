package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.PeriodeInscriptionResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.entity.PeriodeInscription;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PeriodeInscriptionMapper implements Mapper<PeriodeInscription, PeriodeInscriptionResponse> {

    private final AnneeScolaireRepository anneeScolaireRepository;

    @Override
    public PeriodeInscriptionResponse toResponse(PeriodeInscription e) {
        if (e == null) return null;
        String anneeLibelle = anneeScolaireRepository.findById(e.getAnneeScolaireId())
                .map(AnneeScolaire::getLibelle).orElse("—");
        return new PeriodeInscriptionResponse(
                e.getId(), e.getUuid(), e.getLibelle(),
                e.getAnneeScolaireId(), anneeLibelle,
                e.getDateOuverture(), e.getDateCloture(),
                e.getActif(), calculerStatut(e),
                e.getModifierLe()
        );
    }

    private String calculerStatut(PeriodeInscription e) {
        if (Boolean.FALSE.equals(e.getActif())) return "INACTIF";
        LocalDate today = LocalDate.now();
        if (today.isBefore(e.getDateOuverture())) return "A_VENIR";
        if (today.isAfter(e.getDateCloture()))    return "CLOTURE";
        return "OUVERT";
    }
}
