package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HouseBlDescRepository extends JpaRepository<HouseBlDescJpaEntity, Long> {

    Optional<HouseBlDescJpaEntity> findByHouseBl_HouseBlId(Long houseBlId);

    void deleteByHouseBl_HouseBlId(Long houseBlId);
}
