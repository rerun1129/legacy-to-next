package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseBlTruckRepository extends JpaRepository<HouseBlTruckJpaEntity, Long> {
    Optional<HouseBlTruckJpaEntity> findByHouseBlHouseBlId(Long houseBlId);
}
