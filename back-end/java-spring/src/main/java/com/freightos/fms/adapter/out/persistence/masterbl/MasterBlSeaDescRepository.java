package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterBlSeaDescRepository extends JpaRepository<MasterBlSeaDescJpaEntity, Long> {

    Optional<MasterBlSeaDescJpaEntity> findBySea_MasterBlSeaId(Long masterBlSeaId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlSeaDescJpaEntity x where x.sea.masterBlSeaId = :id")
    void deleteBySea_MasterBlSeaId(@Param("id") Long id);
}
