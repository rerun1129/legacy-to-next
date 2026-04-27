package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterBlRepository extends JpaRepository<MasterBlJpaEntity, Long> {

    Optional<MasterBlJpaEntity> findByMblNo(String mblNo);

    boolean existsByMblNo(String mblNo);

    Page<MasterBlJpaEntity> findAllByBound(Bound bound, Pageable pageable);
}
