package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.Menu;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends BaseRepository<Menu> {
    List<Menu> findDistinctByListeProfil_IdInOrderByOrdreAsc(List<Long> profilIds);
}