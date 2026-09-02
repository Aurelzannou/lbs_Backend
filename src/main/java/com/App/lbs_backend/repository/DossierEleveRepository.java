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
    @Query("SELECT d FROM DossierEleve d WHERE lower(d.numero) LIKE lower(concat('%', :filter, '%')) OR lower(d.nom) LIKE lower(concat('%', :filter, '%')) OR lower(d.prenom) LIKE lower(concat('%', :filter, '%'))")
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
    @Query("""
        SELECT d FROM DossierEleve d
        WHERE (:anneeId IS NULL OR d.anneeScolaireId = :anneeId)
          AND (:filter IS NULL OR :filter = ''
               OR lower(d.numero) LIKE lower(concat('%', :filter, '%'))
               OR lower(d.nom) LIKE lower(concat('%', :filter, '%'))
               OR lower(d.prenom) LIKE lower(concat('%', :filter, '%')))
        ORDER BY d.id DESC
        """)
    org.springframework.data.domain.Page<DossierEleve> searchFiltered(
        @Param("anneeId") Long anneeId,
        @Param("filter")  String filter,
        org.springframework.data.domain.Pageable pageable
    );

    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut", "etapeCourante", "typeOperation"})
    List<DossierEleve> findByStatutId(Long statutId);

    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut"})
    @Query("""
        SELECT d FROM DossierEleve d
        WHERE d.statut.id = :statutId
          AND (:anneeId IS NULL OR d.anneeScolaireId = :anneeId)
          AND (:classeId IS NULL OR d.classeId = :classeId)
          AND (:filter IS NULL OR :filter = ''
               OR lower(d.numero) LIKE lower(concat('%', :filter, '%'))
               OR lower(d.nom) LIKE lower(concat('%', :filter, '%'))
               OR lower(d.prenom) LIKE lower(concat('%', :filter, '%')))
        ORDER BY d.id DESC
        """)
    List<DossierEleve> findByStatutIdFiltered(
        @Param("statutId") Long statutId,
        @Param("anneeId")  Long anneeId,
        @Param("classeId") Long classeId,
        @Param("filter")   String filter
    );

    @EntityGraph(attributePaths = {"eleve", "classe", "anneeScolaire", "statut"})
    @Query("SELECT d FROM DossierEleve d WHERE d.tuteurId = :tuteurId ORDER BY d.id DESC")
    List<DossierEleve> findByTuteurId(@Param("tuteurId") Long tuteurId);

    /** Un élève a-t-il déjà un dossier « vivant » (ni refusé ni annulé) pour une année scolaire
        donnée ? Sert à empêcher une seconde réinscription sur la même période d'inscription
        (une période d'inscription est rattachée à une seule année scolaire). */
    @Query("""
        SELECT COUNT(d) > 0 FROM DossierEleve d
        WHERE d.eleveId = :eleveId
          AND d.anneeScolaireId = :anneeScolaireId
          AND (d.statut IS NULL OR upper(d.statut.code) NOT IN ('REFUSE', 'ANNULE'))
        """)
    boolean existsDossierVivantPourEleveEtAnnee(@Param("eleveId") Long eleveId,
                                                @Param("anneeScolaireId") Long anneeScolaireId);

    /** Même contrôle mais par IDENTITÉ (nom + prénom), pour bloquer aussi une « nouvelle
        inscription » d'un enfant qui a déjà un dossier cette année (l'Eleve n'existe pas encore
        au dépôt, donc on ne peut pas comparer sur eleveId). `excludeDossierId` permet d'exclure
        le dossier lui-même lors d'une modification. */
    @Query("""
        SELECT COUNT(d) > 0 FROM DossierEleve d
        WHERE d.anneeScolaireId = :anneeScolaireId
          AND (:excludeDossierId IS NULL OR d.id <> :excludeDossierId)
          AND lower(trim(d.nom)) = lower(trim(:nom))
          AND lower(trim(d.prenom)) = lower(trim(:prenom))
          AND (d.statut IS NULL OR upper(d.statut.code) NOT IN ('REFUSE', 'ANNULE'))
        """)
    boolean existsDossierVivantPourIdentiteEtAnnee(@Param("nom") String nom,
                                                   @Param("prenom") String prenom,
                                                   @Param("anneeScolaireId") Long anneeScolaireId,
                                                   @Param("excludeDossierId") Long excludeDossierId);
}
