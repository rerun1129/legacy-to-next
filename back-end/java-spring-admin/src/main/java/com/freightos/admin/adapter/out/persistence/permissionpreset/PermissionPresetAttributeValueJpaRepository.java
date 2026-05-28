package com.freightos.admin.adapter.out.persistence.permissionpreset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissionPresetAttributeValueJpaRepository
        extends JpaRepository<PermissionPresetAttributeValueJpaEntity, Long> {

    List<PermissionPresetAttributeValueJpaEntity> findAllByPresetId(Long presetId);

    boolean existsByPresetIdAndAttributeKeyAndAvValue(Long presetId, String attributeKey, String avValue);

    @Modifying
    @Query("""
            DELETE FROM PermissionPresetAttributeValueJpaEntity e
            WHERE e.presetId = :presetId
              AND e.attributeKey = :attributeKey
              AND e.avValue = :avValue
            """)
    void deleteByPresetIdAndAttributeKeyAndAvValue(@Param("presetId") Long presetId,
                                                   @Param("attributeKey") String attributeKey,
                                                   @Param("avValue") String avValue);
}
