package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseBlScheduleLegJpaRepository extends JpaRepository<HouseBlScheduleLegJpaEntity, Long> {

    List<HouseBlScheduleLegJpaEntity> findByHouseBlHouseBlIdOrderBySeqAsc(Long houseBlId);

    void deleteByHouseBlHouseBlId(Long houseBlId);
}
