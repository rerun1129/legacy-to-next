package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlNonBlJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseBlNonBlRepository extends JpaRepository<HouseBlNonBlJpaEntity, Long> {

    Optional<HouseBlNonBlJpaEntity> findByHouseBlHouseBlId(Long houseBlId);
}
