package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HouseBlRepository extends JpaRepository<HouseBlJpaEntity, Long> {

    Optional<HouseBlJpaEntity> findByHblNo(String hblNo);

    boolean existsByHblNo(String hblNo);

    /** 리스트 화면 기본 조회: jobDiv + bound 필터 */
    Page<HouseBlJpaEntity> findAllByJobDivAndBoundOrderByCreatedAtDesc(
            JobDiv jobDiv, Bound bound, Pageable pageable);

    /** ETD 범위 조회 (타임라인 위젯 등) */
    @Query("""
            SELECT h FROM HouseBlJpaEntity h
            WHERE h.jobDiv = :jobDiv
              AND h.bound  = :bound
              AND h.etd BETWEEN :from AND :to
            ORDER BY h.etd ASC
            """)
    Page<HouseBlJpaEntity> findBySchedule(@Param("jobDiv") JobDiv jobDiv,
                                          @Param("bound")  Bound  bound,
                                          @Param("from")   String from,
                                          @Param("to")     String to,
                                          Pageable pageable);

    /** Master B/L 연결 건수 조회 */
    long countByMasterBlId(Long masterBlId);
}
