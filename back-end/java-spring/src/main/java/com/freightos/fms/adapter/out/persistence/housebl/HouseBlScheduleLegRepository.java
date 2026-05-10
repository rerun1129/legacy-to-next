package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseBlScheduleLegRepository extends JpaRepository<HouseBlScheduleLegJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlScheduleLegJpaEntity l " +
           "where l.houseBlAirId in (" +
           "  select a.houseBlAirId from HouseBlAirJpaEntity a where a.houseBl.houseBlId = :parentId)")
    void deleteByParentHouseBlId(@Param("parentId") Long parentId);
}
