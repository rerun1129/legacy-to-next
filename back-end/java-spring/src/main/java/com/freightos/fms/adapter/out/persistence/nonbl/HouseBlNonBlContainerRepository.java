package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseBlNonBlContainerRepository extends JpaRepository<HouseBlNonBlContainerJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlNonBlContainerJpaEntity c " +
           "where c.houseBlNonBlId in (" +
           "  select n.houseBlNonBlId from HouseBlNonBlJpaEntity n where n.houseBl.houseBlId = :parentId)")
    void deleteByParentHouseBlId(@Param("parentId") Long parentId);
}
