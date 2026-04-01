package com.App.lbs_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface de base pour les signatures des fonctions DAO communes à tous les
 * repositories<br>
 * NB : L'annotation @NoRepositoryBean s'utilise dans le cas ou la classe fille
 * implémentant la JpaRepository ne définit pas les types paramétrisés
 * <br><a href="https://github.com/spring-projects/spring-data-jpa/issues/2969">Spring Data JPA generates incorrect JPQL query for pagination</a>
 *
 * @param <T> Represents the entity to be injected
 * @author loick
 * @since Mars 2026
 */
@NoRepositoryBean
@Transactional(readOnly = true)
public interface BaseRepository<T> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    @Query("SELECT u FROM #{#entityName} u WHERE u.code = :code")
    Optional<T> findByCode(@Param("code") String code);

    @Query("SELECT u FROM #{#entityName} u WHERE u.uuid = :uuid")
    Optional<T> findByUuid(@Param("uuid") String uuid);

    @Query("SELECT u FROM #{#entityName} u WHERE upper(u.code) = upper(:code)")
    Optional<T> findByStrictCode(@Param("code") String code);

    @Query(value = "SELECT u FROM #{#entityName} u WHERE lower(u.code) like LOWER(concat('%',:filter,'%')) ")
    Page<T> findByLabelContaining(@Param("filter") String label, Pageable pageable);

    @Query(value = "SELECT u FROM #{#entityName} u WHERE u.code IN :codes")
    Page<T> findByCodeIn(@Param("codes") Collection<String> codes, Pageable pageable);

    @Query("SELECT u.id FROM #{#entityName} u WHERE u.uuid IN :uuids")
    List<Long> getIds(@Param("uuids") List<String> ids);

    @Query("SELECT COUNT(u) > 0 FROM #{#entityName} u WHERE u.code = :code")
    boolean existsByCode(@Param("code") String code);
}
