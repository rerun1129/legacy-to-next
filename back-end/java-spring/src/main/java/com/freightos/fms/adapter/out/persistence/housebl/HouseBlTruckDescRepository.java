package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseBlTruckDescRepository extends JpaRepository<HouseBlTruckDescJpaEntity, Long> {

    Optional<HouseBlTruckDescJpaEntity> findByTruck_HouseBlTruckId(Long houseBlTruckId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlTruckDescJpaEntity x where x.truck.houseBlTruckId = :id")
    void deleteByTruck_HouseBlTruckId(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlTruckDescJpaEntity x " +
           "where x.truck.houseBlTruckId in (" +
           "  select t.houseBlTruckId from HouseBlTruckJpaEntity t where t.houseBl.houseBlId = :parentId)")
    void deleteByParentHouseBlId(@Param("parentId") Long parentId);
}
