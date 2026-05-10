package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaContainerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseBlSeaContainerRepository extends JpaRepository<HouseBlSeaContainerJpaEntity, Long> {

    @Modifying(flushAutomatically = true)
    @Query("delete from HouseBlSeaContainerJpaEntity c " +
           "where c.houseBlSeaId in (" +
           "  select s.houseBlSeaId from HouseBlSeaJpaEntity s where s.houseBl.houseBlId = :parentId)")
    void deleteByParentHouseBlId(@Param("parentId") Long parentId);
}
