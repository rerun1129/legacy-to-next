package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HouseBlNonBlRepository extends JpaRepository<HouseBlNonBlJpaEntity, Long> {

    Optional<HouseBlNonBlJpaEntity> findByHouseBlHouseBlId(Long houseBlId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlNonBlJpaEntity x where x.houseBl.houseBlId = :id")
    void deleteByHouseBl_HouseBlId(@Param("id") Long id);
}
