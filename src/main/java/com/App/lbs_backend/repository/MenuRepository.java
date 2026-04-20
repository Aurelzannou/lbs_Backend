package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends BaseRepository<Menu> {
    List<Menu> findDistinctByListeProfilMenu_Profil_IdInOrderByOrdreAsc(List<Long> profilIds);

    @Override
    @Query("SELECT m FROM Menu m WHERE " +
           "LOWER(m.titre) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(m.code) LIKE LOWER(CONCAT('%', :filter, '%')) OR " +
           "LOWER(m.path) LIKE LOWER(CONCAT('%', :filter, '%'))")
    Page<Menu> findByLabelContaining(@Param("filter") String filter, Pageable pageable);
}