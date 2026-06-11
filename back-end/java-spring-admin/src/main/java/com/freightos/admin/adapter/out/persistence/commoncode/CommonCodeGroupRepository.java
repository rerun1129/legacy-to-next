package com.freightos.admin.adapter.out.persistence.commoncode;

import com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommonCodeGroupRepository extends JpaRepository<CommonCodeGroupJpaEntity, Long> {

    boolean existsByGroupCode(String groupCode);

    @Query("SELECT new com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary(" +
           "g.commonCodeGroupId, g.groupCode, g.sourceModule, g.description, g.active) " +
           "FROM CommonCodeGroupJpaEntity g ORDER BY g.sourceModule, g.groupCode")
    List<CommonCodeGroupSummary> findAllGroupSummaries();
}
