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

import java.time.LocalDateTime;

/**
 * admin.carrier 읽기 전용 참조 엔티티.
 * PMS 조회 시 liner_code → carrier name 변환에만 사용.
 */
@Entity
@Immutable
@Table(schema = "admin", name = "carrier")
@Getter
@NoArgsConstructor
public class PmsCarrierRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrier_id")
    private Long id;

    @Column(name = "carrier_code", nullable = false, length = 20)
    private String carrierCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
