package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HouseBlTruckRepository extends JpaRepository<HouseBlTruckJpaEntity, Long> {

    Optional<HouseBlTruckJpaEntity> findByHouseBlHouseBlId(Long houseBlId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from HouseBlTruckJpaEntity x where x.houseBl.houseBlId = :id")
    void deleteByHouseBl_HouseBlId(@Param("id") Long id);
}
