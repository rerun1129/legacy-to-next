package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseBlScheduleLegJpaRepository extends JpaRepository<HouseBlScheduleLegJpaEntity, Long> {

    // seq 필드 없음 — PK 오름차순으로 삽입 순서 보장
    List<HouseBlScheduleLegJpaEntity> findByHouseBlHouseBlIdOrderByHouseBlScheduleLegIdAsc(Long houseBlId);
}
