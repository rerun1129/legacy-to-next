package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MasterBlAirRepository extends JpaRepository<MasterBlAirJpaEntity, Long> {

    Optional<MasterBlAirJpaEntity> findByMasterBlMasterBlId(Long masterBlId);

    @Modifying(flushAutomatically = true)
    @Query("delete from MasterBlAirJpaEntity x where x.masterBl.masterBlId = :id")
    void deleteByMasterBl_MasterBlId(@Param("id") Long id);
}
