package com.freightos.admin.adapter.out.persistence.commoncode;

import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCodeJpaEntity, Long> {

    boolean existsByGroupCodeAndCode(String groupCode, String code);

    @Query("SELECT new com.freightos.admin.application.commoncode.projection.CommonCodeSummary(" +
           "c.commonCodeId, c.groupCode, c.code, c.label, c.labelKo, c.sortOrder, c.active) " +
           "FROM CommonCodeJpaEntity c WHERE c.groupCode = :groupCode " +
           "ORDER BY c.sortOrder, c.commonCodeId")
    List<CommonCodeSummary> findCodeSummariesByGroupCode(@Param("groupCode") String groupCode);

    @Query("SELECT new com.freightos.admin.application.commoncode.projection.CommonCodeSummary(" +
           "c.commonCodeId, c.groupCode, c.code, c.label, c.labelKo, c.sortOrder, c.active) " +
           "FROM CommonCodeJpaEntity c " +
           "WHERE c.groupCode = :groupCode AND c.active = TRUE " +
           "ORDER BY c.sortOrder, c.commonCodeId")
    List<CommonCodeSummary> findActiveCodeSummariesByGroupCodeOrdered(@Param("groupCode") String groupCode);
}
