package com.App.lbs_backend.mapper;

import com.App.lbs_backend.dto.response.PeriodeRentreeResponse;
import com.App.lbs_backend.entity.AnneeScolaire;
import com.App.lbs_backend.entity.PeriodeRentree;
import com.App.lbs_backend.repository.AnneeScolaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class PeriodeRentreeMapper implements Mapper<PeriodeRentree, PeriodeRentreeResponse> {

    private final AnneeScolaireRepository anneeScolaireRepository;

    @Override
    public PeriodeRentreeResponse toResponse(PeriodeRentree e) {
        if (e == null) return null;
        String anneeLibelle = anneeScolaireRepository.findById(e.getAnneeScolaireId())
                .map(AnneeScolaire::getLibelle).orElse("—");
        return new PeriodeRentreeResponse(
                e.getId(), e.getUuid(), e.getLibelle(),
                e.getAnneeScolaireId(), anneeLibelle,
                e.getDateOuverture(), e.getDateCloture(),
                e.getActif(), calculerStatut(e),
                e.getModifierLe()
        );
    }

    private String calculerStatut(PeriodeRentree e) {
        if (Boolean.FALSE.equals(e.getActif())) return "INACTIF";
        LocalDate today = LocalDate.now();
        if (today.isBefore(e.getDateOuverture())) return "A_VENIR";
        if (today.isAfter(e.getDateCloture()))    return "CLOTURE";
        return "OUVERT";
    }
}
