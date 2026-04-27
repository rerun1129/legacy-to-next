package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseBlSeaRepository extends JpaRepository<HouseBlSeaJpaEntity, Long> {

    Optional<HouseBlSeaJpaEntity> findByHouseBlHouseBlId(Long houseBlId);
}
