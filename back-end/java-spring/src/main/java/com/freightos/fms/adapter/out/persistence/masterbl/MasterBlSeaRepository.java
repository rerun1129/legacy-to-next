package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterBlSeaRepository extends JpaRepository<MasterBlSeaJpaEntity, Long> {

    Optional<MasterBlSeaJpaEntity> findByMasterBlMasterBlId(Long masterBlId);
}
