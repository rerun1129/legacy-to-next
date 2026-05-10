package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseBlSeaDescRepository extends JpaRepository<HouseBlSeaDescJpaEntity, Long> {

    Optional<HouseBlSeaDescJpaEntity> findBySea_HouseBlSeaId(Long houseBlSeaId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlSeaDescJpaEntity x where x.sea.houseBlSeaId = :id")
    void deleteBySea_HouseBlSeaId(@Param("id") Long id);
}
