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

    /** Toutes les notes de toute une classe pour la période demandée, en une requête — nécessaire
        pour calculer les rangs (comparaison entre camarades de classe). Filtrée par classe : un
        élève ayant changé de classe en cours d'année ne doit pas ramener, pour CETTE période, des
        notes saisies sous une autre classe. */
    List<Note> findByEleveIdInAndPeriodeIdAndClasseId(List<Long> eleveIds, Long periodeId, Long classeId);

    /** Variante non filtrée par classe — sert uniquement au calcul de la moyenne annuelle, qui doit
        agréger l'historique complet d'un élève sur TOUTES les périodes de l'année, y compris celles
        où il était dans une autre classe (une mutation de classe en cours d'année ne doit pas faire
        disparaître ses moyennes des trimestres précédents). */
    List<Note> findByEleveIdInAndPeriodeId(List<Long> eleveIds, Long periodeId);

    /** Roster d'une classe pour une matière/période donnée — alimente la feuille de saisie. */
    List<Note> findByEleveIdInAndMatiereIdAndPeriodeIdAndClasseId(
            List<Long> eleveIds, Long matiereId, Long periodeId, Long classeId);

    /** Clé naturelle d'upsert : un élève ne peut avoir qu'une seule note par (classe, matière,
        période, type d'évaluation, numéro) — la classe fait partie de la clé pour qu'un nouveau
        professeur (nouvelle classe) ne puisse jamais écraser silencieusement une note saisie sous
        une autre classe pour le même élève/matière/période. */
    Optional<Note> findByEleveIdAndClasseIdAndMatiereIdAndPeriodeIdAndTypeEvaluationAndNumero(
            Long eleveId, Long classeId, Long matiereId, Long periodeId, String typeEvaluation, Integer numero);

    /** Numéro le plus élevé déjà utilisé pour un type d'évaluation donné sur une classe/matière/
        période — permet de savoir combien de colonnes d'interrogations existent déjà, pour ne pas
        en perdre si le professeur en avait saisi plus lors d'un enregistrement précédent. */
    @Query("""
        SELECT MAX(n.numero) FROM Note n
        WHERE n.eleveId IN :eleveIds AND n.classeId = :classeId AND n.matiereId = :matiereId
          AND n.periodeId = :periodeId AND n.typeEvaluation = :typeEvaluation
        """)
    Integer findMaxNumero(@Param("eleveIds") List<Long> eleveIds, @Param("classeId") Long classeId,
                          @Param("matiereId") Long matiereId, @Param("periodeId") Long periodeId,
                          @Param("typeEvaluation") String typeEvaluation);

    /** Triplets (classeId, matiereId, periodeId) ayant au moins une note saisie, pour les périodes
        données — sert au tableau de bord à repérer les matières « en cours de saisie ». */
    @Query("""
        SELECT DISTINCT n.classeId, n.matiereId, n.periodeId FROM Note n
        WHERE n.periodeId IN :periodeIds AND n.valeur IS NOT NULL
        """)
    List<Object[]> findTripletsAvecNotes(@Param("periodeIds") java.util.Collection<Long> periodeIds);

    /** Pour une période : (classeId, matiereId, typeEvaluation, plus grand numéro noté) — permet
        de savoir si le professeur a verrouillé toutes ses colonnes. */
    @Query("""
        SELECT n.classeId, n.matiereId, n.typeEvaluation, MAX(n.numero) FROM Note n
        WHERE n.periodeId = :periodeId AND n.valeur IS NOT NULL
        GROUP BY n.classeId, n.matiereId, n.typeEvaluation
        """)
    List<Object[]> findMaxNumeroParMatiere(@Param("periodeId") Long periodeId);
}
