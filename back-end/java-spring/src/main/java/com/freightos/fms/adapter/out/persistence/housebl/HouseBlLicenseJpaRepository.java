package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlLicenseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseBlLicenseJpaRepository extends JpaRepository<HouseBlLicenseJpaEntity, Long> {

    List<HouseBlLicenseJpaEntity> findByHouseBlHouseBlIdOrderBySeqAsc(Long houseBlId);

    void deleteByHouseBlHouseBlId(Long houseBlId);
}
