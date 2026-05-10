package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseBlDescRepository extends JpaRepository<HouseBlDescJpaEntity, Long> {

    Optional<HouseBlDescJpaEntity> findByHouseBl_HouseBlId(Long houseBlId);

    // derived deleteBy는 SELECT-then-delete로 동작 — bulk DELETE로 교체하여 SELECT 제거
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from HouseBlDescJpaEntity x where x.houseBl.houseBlId = :id")
    void deleteByHouseBl_HouseBlId(@Param("id") Long id);
}
