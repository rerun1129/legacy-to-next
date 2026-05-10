package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseBlAirRepository extends JpaRepository<HouseBlAirJpaEntity, Long> {

    Optional<HouseBlAirJpaEntity> findByHouseBlHouseBlId(Long houseBlId);

    void deleteByHouseBl_HouseBlId(Long id);
}
