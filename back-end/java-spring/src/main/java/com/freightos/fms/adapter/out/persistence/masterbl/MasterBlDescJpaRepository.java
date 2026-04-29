package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDescJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterBlDescJpaRepository extends JpaRepository<MasterBlDescJpaEntity, Long> {

    Optional<MasterBlDescJpaEntity> findByMasterBlMasterBlId(Long masterBlId);
}
