package com.freightos.admin.adapter.out.persistence.permissionpreset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PermissionPresetAttributeValueJpaRepository
        extends JpaRepository<PermissionPresetAttributeValueJpaEntity, Long> {

    List<PermissionPresetAttributeValueJpaEntity> findAllByPresetId(Long presetId);

    boolean existsByPresetIdAndAttributeValueId(Long presetId, Long attributeValueId);

    @Modifying
    @Query("""
            DELETE FROM PermissionPresetAttributeValueJpaEntity e
            WHERE e.presetId = :presetId
              AND e.attributeValueId IN :attributeValueIds
            """)
    void deleteByPresetIdAndAttributeValueIdIn(@Param("presetId") Long presetId,
                                               @Param("attributeValueIds") Collection<Long> attributeValueIds);
}
