package com.freightos.fms.adapter.out.persistence.switchbl;

import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlDescriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SwitchBlDescriptionJpaRepository extends JpaRepository<SwitchBlDescriptionJpaEntity, Long> {
    Optional<SwitchBlDescriptionJpaEntity> findBySwitchBlSwitchBlId(Long switchBlId);
}
