package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckOrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseBlTruckOrderRepository extends JpaRepository<HouseBlTruckOrderJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlTruckOrderJpaEntity o " +
           "where o.houseBlTruckId in (" +
           "  select t.houseBlTruckId from HouseBlTruckJpaEntity t where t.houseBl.houseBlId = :parentId)")
    void deleteByParentHouseBlId(@Param("parentId") Long parentId);
}
