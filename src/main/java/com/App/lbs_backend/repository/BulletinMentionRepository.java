package com.App.lbs_backend.repository;

import com.App.lbs_backend.entity.BulletinMention;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BulletinMentionRepository extends BaseRepository<BulletinMention> {
    Optional<BulletinMention> findByEleveIdAndPeriodeId(Long eleveId, Long periodeId);
    List<BulletinMention> findByEleveIdInAndPeriodeId(List<Long> eleveIds, Long periodeId);
}
