package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MasterBlRepository extends JpaRepository<MasterBlJpaEntity, Long>, MasterBlRepositoryCustom {

    Optional<MasterBlJpaEntity> findByMblNo(String mblNo);

    boolean existsByMblNo(String mblNo);

    Page<MasterBlJpaEntity> findAllByBound(Bound bound, Pageable pageable);

    /** jobDiv만 가벼운 projection — delete 흐름에서 SELECT 1회(도메인 전체 로드 X) */
    @Query("select m.jobDiv from MasterBlJpaEntity m where m.masterBlId = :id")
    Optional<MasterBlJobDiv> findJobDivById(@Param("id") Long id);

    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlJpaEntity m where m.masterBlId = :id")
    void deleteByIdBulk(@Param("id") Long id);
}
