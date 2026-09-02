package com.App.lbs_backend.service.referentiel;

import com.App.lbs_backend.core.AbstractBaseService;
import com.App.lbs_backend.dto.response.ClasseResponse;
import com.App.lbs_backend.entity.Classe;
import com.App.lbs_backend.entity.Coefficient;
import com.App.lbs_backend.mapper.ClasseMapper;
import com.App.lbs_backend.mapper.Mapper;
import com.App.lbs_backend.repository.BaseRepository;
import com.App.lbs_backend.repository.ClasseRepository;
import com.App.lbs_backend.repository.CoefficientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ClasseService extends AbstractBaseService<Classe, ClasseResponse> {

    private final ClasseRepository classeRepository;
    private final ClasseMapper classeMapper;
    private final CoefficientRepository coefficientRepository;

    public ClasseService(ClasseRepository classeRepository, ClasseMapper classeMapper,
                         CoefficientRepository coefficientRepository) {
        super(Classe.class);
        this.classeRepository = classeRepository;
        this.classeMapper = classeMapper;
        this.coefficientRepository = coefficientRepository;
    }

    /** Enregistre / met à jour les coefficients (matière → valeur) pour le niveau donné.
        Appelé depuis le formulaire de la classe : dans un niveau, une matière a un coefficient. */
    @Transactional
    public void synchroniserCoefficients(Long niveauId, Map<Long, Double> coefParMatiere) {
        if (niveauId == null || coefParMatiere == null) return;
        coefParMatiere.forEach((matiereId, valeur) -> {
            if (matiereId == null) return;
            double v = valeur != null && valeur > 0 ? valeur : 1.0;
            Coefficient c = coefficientRepository.findByNiveauIdAndMatiereId(niveauId, matiereId)
                    .orElseGet(() -> {
                        Coefficient nouveau = new Coefficient();
                        nouveau.setNiveauId(niveauId);
                        nouveau.setMatiereId(matiereId);
                        return nouveau;
                    });
            c.setValeur(v);
            coefficientRepository.save(c);
        });
    }

    @Override
    public BaseRepository<Classe> repository() {
        return classeRepository;
    }

    @Override
    public Mapper<Classe, ClasseResponse> mapper() {
        return classeMapper;
    }
}
