package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlReferenceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseBlReferenceJpaRepository extends JpaRepository<HouseBlReferenceJpaEntity, Long> {

    List<HouseBlReferenceJpaEntity> findByHouseBlHouseBlIdOrderBySeqAsc(Long houseBlId);

    void deleteByHouseBlHouseBlId(Long houseBlId);
}
