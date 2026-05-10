package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MasterBlDimRepository extends JpaRepository<MasterBlDimJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlDimJpaEntity d " +
           "where d.masterBlAirId in (" +
           "  select a.masterBlAirId from MasterBlAirJpaEntity a where a.masterBl.masterBlId = :parentId)")
    void deleteByParentMasterBlId(@Param("parentId") Long parentId);
}
