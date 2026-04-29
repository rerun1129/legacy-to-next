package com.freightos.fms.adapter.out.persistence.switchbl;

import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SwitchBlJpaRepository extends JpaRepository<SwitchBlJpaEntity, Long> {
    Optional<SwitchBlJpaEntity> findByHouseBlHouseBlId(Long houseBlId);
}
