package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterBlScheduleLegJpaRepository extends JpaRepository<MasterBlScheduleLegJpaEntity, Long> {

    List<MasterBlScheduleLegJpaEntity> findByMasterBlMasterBlIdOrderBySeqAsc(Long masterBlId);

    void deleteByMasterBlMasterBlId(Long masterBlId);
}
