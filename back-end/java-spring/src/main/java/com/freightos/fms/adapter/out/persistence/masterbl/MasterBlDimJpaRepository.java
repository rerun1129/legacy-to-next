package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterBlDimJpaRepository extends JpaRepository<MasterBlDimJpaEntity, Long> {

    List<MasterBlDimJpaEntity> findByMasterBlMasterBlIdOrderBySeqAsc(Long masterBlId);

    void deleteByMasterBlMasterBlId(Long masterBlId);
}
