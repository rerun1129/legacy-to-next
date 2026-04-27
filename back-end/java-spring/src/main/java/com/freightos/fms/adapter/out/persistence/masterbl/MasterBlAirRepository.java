package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterBlAirRepository extends JpaRepository<MasterBlAirJpaEntity, Long> {
    Optional<MasterBlAirJpaEntity> findByMasterBlMasterBlId(Long masterBlId);
}
