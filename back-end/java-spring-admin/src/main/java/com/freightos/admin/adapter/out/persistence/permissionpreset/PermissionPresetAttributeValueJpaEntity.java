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
 * attribute_value 의 PK 가 (attribute_key, value) 복합키이므로 두 컬럼을 모두 보유한다.
 * audit 컬럼 없음 — 단순 연결 테이블이므로 BaseJpaEntity 미상속.
 */
@Entity
@Table(schema = "admin", name = "permission_preset_attribute_value",
        uniqueConstraints = @UniqueConstraint(name = "uq_ppav_preset_av", columnNames = {"preset_id", "attribute_key", "av_value"}))
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

    @Column(name = "attribute_key", nullable = false, length = 80)
    private String attributeKey;

    @Column(name = "av_value", nullable = false, length = 100)
    private String avValue;

    public static PermissionPresetAttributeValueJpaEntity of(Long presetId, String attributeKey, String avValue) {
        PermissionPresetAttributeValueJpaEntity e = new PermissionPresetAttributeValueJpaEntity();
        e.presetId     = presetId;
        e.attributeKey = attributeKey;
        e.avValue      = avValue;
        return e;
    }
}
