package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseBlDescJpaRepository extends JpaRepository<HouseBlDescJpaEntity, Long> {

    Optional<HouseBlDescJpaEntity> findByHouseBlHouseBlId(Long houseBlId);
}
