package com.freightos.bms.adapter.out.persistence.codename.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * admin.admin_user 읽기 전용 참조 엔티티.
 * BMS 조회 시 operator(username) → 표시명 변환에만 사용.
 * cross-schema 접근은 codename 패키지에만 격리.
 *
 * admin.admin_user에는 'name' 컬럼이 없고 V50에서 추가된 'user_eng_name'이 표시명으로 사용됨.
 * deleted_at IS NULL 활성 유저만 조회.
 */
@Entity
@Immutable
@Table(schema = "admin", name = "admin_user")
@Getter
@NoArgsConstructor
public class AdminUserRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "user_eng_name", length = 100)
    private String userEngName;
}
