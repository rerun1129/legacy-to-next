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

import java.time.LocalDateTime;

/**
 * admin.freight 읽기 전용 참조 엔티티.
 * BMS 조회 시 freight_code → name 변환에만 사용.
 * cross-schema 접근은 codename 패키지에만 격리.
 */
@Entity
@Immutable
@Table(schema = "admin", name = "freight")
@Getter
@NoArgsConstructor
public class FreightRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_id")
    private Long id;

    @Column(name = "freight_code", nullable = false, length = 20)
    private String freightCode;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
