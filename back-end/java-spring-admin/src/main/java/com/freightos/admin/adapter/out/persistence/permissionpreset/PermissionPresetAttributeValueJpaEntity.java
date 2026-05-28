package com.freightos.admin.adapter.out.persistence.permissionpreset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * permission_preset_attribute_value 매핑 테이블 엔티티.
 * V34 이후 attribute_value_id 단일 FK 참조로 단순화되었다.
 * audit 컬럼 없음 — 단순 연결 테이블이므로 BaseJpaEntity 미상속.
 */
@Entity
@Table(schema = "admin", name = "permission_preset_attribute_value",
        uniqueConstraints = @UniqueConstraint(name = "uq_ppav_preset_av", columnNames = {"preset_id", "attribute_value_id"}))
@Getter
@Setter
@NoArgsConstructor
public class PermissionPresetAttributeValueJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "preset_id", nullable = false)
    private Long presetId;

    @Column(name = "attribute_value_id", nullable = false)
    private Long attributeValueId;

    public static PermissionPresetAttributeValueJpaEntity of(Long presetId, Long attributeValueId) {
        PermissionPresetAttributeValueJpaEntity e = new PermissionPresetAttributeValueJpaEntity();
        e.presetId          = presetId;
        e.attributeValueId  = attributeValueId;
        return e;
    }
}
