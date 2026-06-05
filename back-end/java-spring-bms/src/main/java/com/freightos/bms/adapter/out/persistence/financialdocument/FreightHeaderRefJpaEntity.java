package com.freightos.bms.adapter.out.persistence.financialdocument;

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
 * bms.freight_header 읽기 전용 참조 엔티티.
 * blType+blId 기반 headerId 조회·필터용.
 * 연관관계 없이 컬럼만 매핑.
 */
@Entity
@Immutable
@Table(schema = "bms", name = "freight_header")
@Getter
@NoArgsConstructor
public class FreightHeaderRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_header_id", updatable = false, nullable = false)
    private Long freightHeaderId;

    @Column(name = "bl_type", nullable = false, length = 10)
    private String blType;

    @Column(name = "bl_id", nullable = false)
    private Long blId;
}
