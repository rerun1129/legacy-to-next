package com.freightos.fms.adapter.out.persistence.codename.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * admin.admin_user 읽기 전용 참조 엔티티.
 * FMS 조회 시 username → display name(user_eng_name COALESCE email) 변환에만 사용.
 * cross-schema 접근은 codename 패키지에만 격리.
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
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /** V50에서 추가된 영문 표시명. null이면 email로 대체. */
    @Column(name = "user_eng_name", length = 100)
    private String userEngName;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
