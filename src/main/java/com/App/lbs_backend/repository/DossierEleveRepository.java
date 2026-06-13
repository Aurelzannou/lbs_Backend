package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.DossierEleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierEleveRepository extends BaseRepository<DossierEleve> {

    String GRAPH = "dossier-eleve-with-relations";

    @Override
    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut", "etapeCourante", "typeOperation"})
    Optional<DossierEleve> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut", "etapeCourante", "typeOperation"})
    @Query("SELECT d FROM DossierEleve d WHERE lower(d.numero) LIKE lower(concat('%', :filter, '%')) OR lower(d.eleve.nom) LIKE lower(concat('%', :filter, '%')) OR lower(d.eleve.prenom) LIKE lower(concat('%', :filter, '%'))")
    Page<DossierEleve> findByLabelContaining(@Param("filter") String filter, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut", "etapeCourante", "typeOperation"})
    @Query("SELECT d FROM DossierEleve d WHERE d.uuid = :uuid")
    Optional<DossierEleve> findByUuid(@Param("uuid") String uuid);

    @Override
    @Query("SELECT d FROM DossierEleve d WHERE d.numero = :code")
    Optional<DossierEleve> findByCode(@Param("code") String code);

    @Override
    @Query("SELECT d FROM DossierEleve d WHERE upper(d.numero) = upper(:code)")
    Optional<DossierEleve> findByStrictCode(@Param("code") String code);

    @Override
    @Query("SELECT COUNT(d) > 0 FROM DossierEleve d WHERE d.numero = :code")
    boolean existsByCode(@Param("code") String code);

    @Override
    @Query("SELECT d FROM DossierEleve d WHERE d.numero IN :codes")
    Page<DossierEleve> findByCodeIn(@Param("codes") java.util.Collection<String> codes, Pageable pageable);

    @Query("SELECT COUNT(d) FROM DossierEleve d WHERE YEAR(d.dateDebut) = :annee")
    long countByAnnee(@Param("annee") int annee);

    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut", "etapeCourante", "typeOperation"})
    List<DossierEleve> findByStatutId(Long statutId);

    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut"})
    @Query("SELECT d FROM DossierEleve d JOIN d.eleve e WHERE e.tuteurId = :tuteurId ORDER BY d.id DESC")
    List<DossierEleve> findByTuteurId(@Param("tuteurId") Long tuteurId);
}
