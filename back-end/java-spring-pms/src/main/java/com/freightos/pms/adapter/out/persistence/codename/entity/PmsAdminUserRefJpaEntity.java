package com.freightos.pms.adapter.out.persistence.codename.entity;

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
 * PMS 조회 시 operator(username) → 표시명(user_eng_name) 변환에만 사용.
 */
@Entity
@Immutable
@Table(schema = "admin", name = "admin_user")
@Getter
@NoArgsConstructor
public class PmsAdminUserRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "user_eng_name", length = 100)
    private String userEngName;
}
