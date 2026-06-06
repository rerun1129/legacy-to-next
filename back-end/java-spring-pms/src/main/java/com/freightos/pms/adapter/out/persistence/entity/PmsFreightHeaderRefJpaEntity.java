package com.freightos.pms.adapter.out.persistence.entity;

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
 * B/L 키(bl_type + bl_id) 및 고객사·운송사 코드 조회에 사용.
 */
@Entity
@Immutable
@Table(schema = "bms", name = "freight_header")
@Getter
@NoArgsConstructor
public class PmsFreightHeaderRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_header_id")
    private Long freightHeaderId;

    @Column(name = "bl_type", length = 10)
    private String blType;

    @Column(name = "bl_id")
    private Long blId;

    @Column(name = "actual_customer_code", length = 40)
    private String actualCustomerCode;

    @Column(name = "settle_partner_code", length = 40)
    private String settlePartnerCode;

    @Column(name = "liner_code", length = 20)
    private String linerCode;
}
