package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.DossierEleve;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierEleveRepository extends BaseRepository<DossierEleve> {

    List<DossierEleve> findByStatutId(Long statutId);

    @Query("SELECT d FROM DossierEleve d JOIN Eleve e ON d.eleveId = e.id WHERE e.tuteurId = :tuteurId ORDER BY d.id DESC")
    List<DossierEleve> findByTuteurId(@Param("tuteurId") Long tuteurId);
}
