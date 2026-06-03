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
 * admin.customer 읽기 전용 참조 엔티티.
 * FMS 조회 시 customer_code → name 변환에만 사용.
 * cross-schema 접근은 codename 패키지에만 격리.
 */
@Entity
@Immutable
@Table(schema = "admin", name = "customer")
@Getter
@NoArgsConstructor
public class CustomerRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "customer_code", nullable = false, length = 40)
    private String customerCode;

    @Column(name = "customer_type", length = 30)
    private String customerType;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
