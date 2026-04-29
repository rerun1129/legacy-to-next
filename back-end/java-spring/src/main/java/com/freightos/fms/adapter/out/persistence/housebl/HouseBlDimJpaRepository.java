package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseBlDimJpaRepository extends JpaRepository<HouseBlDimJpaEntity, Long> {

    List<HouseBlDimJpaEntity> findByHouseBlHouseBlIdOrderBySeqAsc(Long houseBlId);

    void deleteByHouseBlHouseBlId(Long houseBlId);
}
