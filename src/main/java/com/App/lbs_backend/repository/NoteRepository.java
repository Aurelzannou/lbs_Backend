package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Note;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends BaseRepository<Note> {
    /** Toutes les notes d'un élève pour une période — alimente le bulletin d'un seul élève. */
    List<Note> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);

    /** Toutes les notes de toute une classe pour une période, en une requête — nécessaire pour
        calculer les rangs (comparaison entre camarades de classe). */
    List<Note> findByEleveIdInAndPeriodeId(List<Long> eleveIds, Long periodeId);

    /** Roster d'une classe pour une matière/période donnée — alimente la feuille de saisie. */
    List<Note> findByEleveIdInAndMatiereIdAndPeriodeId(List<Long> eleveIds, Long matiereId, Long periodeId);

    /** Clé naturelle d'upsert : un élève ne peut avoir qu'une seule note par (matière, période,
        type d'évaluation, numéro). */
    Optional<Note> findByEleveIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
            Long eleveId, Long matiereId, Long periodeId, String typeEvaluation, Integer numero);

    /** Numéro le plus élevé déjà utilisé pour un type d'évaluation donné sur une classe/matière/
        période — permet de savoir combien de colonnes d'interrogations existent déjà, pour ne pas
        en perdre si le professeur en avait saisi plus lors d'un enregistrement précédent. */
    @Query("""
        SELECT MAX(n.numero) FROM Note n
        WHERE n.eleveId IN :eleveIds AND n.matiereId = :matiereId AND n.periodeId = :periodeId
          AND n.typeEvaluation = :typeEvaluation
        """)
    Integer findMaxNumero(@Param("eleveIds") List<Long> eleveIds, @Param("matiereId") Long matiereId,
                          @Param("periodeId") Long periodeId, @Param("typeEvaluation") String typeEvaluation);
}
