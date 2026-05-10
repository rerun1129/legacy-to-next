package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseBlAirDimRepository extends JpaRepository<HouseBlAirDimJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlAirDimJpaEntity d " +
           "where d.houseBlAirId in (" +
           "  select a.houseBlAirId from HouseBlAirJpaEntity a where a.houseBl.houseBlId = :parentId)")
    void deleteByParentHouseBlId(@Param("parentId") Long parentId);
}
