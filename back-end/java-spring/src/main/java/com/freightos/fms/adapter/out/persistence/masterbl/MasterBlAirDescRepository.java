package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterBlAirDescRepository extends JpaRepository<MasterBlAirDescJpaEntity, Long> {

    Optional<MasterBlAirDescJpaEntity> findByAir_MasterBlAirId(Long masterBlAirId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlAirDescJpaEntity x where x.air.masterBlAirId = :id")
    void deleteByAir_MasterBlAirId(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlAirDescJpaEntity x " +
           "where x.air.masterBlAirId in (" +
           "  select a.masterBlAirId from MasterBlAirJpaEntity a where a.masterBl.masterBlId = :parentId)")
    void deleteByParentMasterBlId(@Param("parentId") Long parentId);
}
