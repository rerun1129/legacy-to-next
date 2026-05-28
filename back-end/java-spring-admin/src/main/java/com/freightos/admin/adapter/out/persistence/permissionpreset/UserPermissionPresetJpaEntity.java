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
 * user_permission_preset 매핑 테이블 엔티티.
 * audit 컬럼 없음 — 단순 연결 테이블이므로 BaseJpaEntity 미상속.
 */
@Entity
@Table(schema = "admin", name = "user_permission_preset",
        uniqueConstraints = @UniqueConstraint(name = "uq_upp_user_preset", columnNames = {"user_id", "preset_id"}))
@Getter
@Setter
@NoArgsConstructor
public class UserPermissionPresetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "preset_id", nullable = false)
    private Long presetId;
}
