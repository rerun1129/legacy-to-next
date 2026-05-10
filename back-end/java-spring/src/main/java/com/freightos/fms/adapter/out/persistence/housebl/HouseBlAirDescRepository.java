package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseBlAirDescRepository extends JpaRepository<HouseBlAirDescJpaEntity, Long> {

    Optional<HouseBlAirDescJpaEntity> findByAir_HouseBlAirId(Long houseBlAirId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlAirDescJpaEntity x where x.air.houseBlAirId = :id")
    void deleteByAir_HouseBlAirId(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlAirDescJpaEntity x " +
           "where x.air.houseBlAirId in (" +
           "  select a.houseBlAirId from HouseBlAirJpaEntity a where a.houseBl.houseBlId = :parentId)")
    void deleteByParentHouseBlId(@Param("parentId") Long parentId);
}
