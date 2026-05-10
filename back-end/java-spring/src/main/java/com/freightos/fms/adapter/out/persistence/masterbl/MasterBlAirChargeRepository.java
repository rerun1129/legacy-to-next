package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirChargeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MasterBlAirChargeRepository extends JpaRepository<MasterBlAirChargeJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlAirChargeJpaEntity c " +
           "where c.masterBlAirId in (" +
           "  select a.masterBlAirId from MasterBlAirJpaEntity a where a.masterBl.masterBlId = :parentId)")
    void deleteByParentMasterBlId(@Param("parentId") Long parentId);
}
