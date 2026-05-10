package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MasterBlSeaRepository extends JpaRepository<MasterBlSeaJpaEntity, Long> {

    Optional<MasterBlSeaJpaEntity> findByMasterBlMasterBlId(Long masterBlId);

    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlSeaJpaEntity x where x.masterBl.masterBlId = :id")
    void deleteByMasterBl_MasterBlId(@Param("id") Long id);
}
