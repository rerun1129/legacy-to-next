package com.freightos.fms.domain.housebl.repository;

import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface HouseBlRepository extends JpaRepository<HouseBl, UUID> {

    Optional<HouseBl> findByHblNo(String hblNo);

    boolean existsByHblNo(String hblNo);

    /** 리스트 화면 기본 조회: jobDiv + bound 필터 */
    Page<HouseBl> findAllByJobDivAndBoundOrderByCreatedAtDesc(
            JobDiv jobDiv, Bound bound, Pageable pageable);

    /** ETD 범위 조회 (타임라인 위젯 등) */
    @Query("""
            SELECT h FROM HouseBl h
            WHERE h.jobDiv = :jobDiv
              AND h.bound  = :bound
              AND h.etd BETWEEN :from AND :to
            ORDER BY h.etd ASC
            """)
    Page<HouseBl> findBySchedule(@Param("jobDiv") JobDiv jobDiv,
                                 @Param("bound")  Bound  bound,
                                 @Param("from")   LocalDate from,
                                 @Param("to")     LocalDate to,
                                 Pageable pageable);

    /** Master B/L 연결 건수 조회 */
    long countByMasterBlId(UUID masterBlId);
}
