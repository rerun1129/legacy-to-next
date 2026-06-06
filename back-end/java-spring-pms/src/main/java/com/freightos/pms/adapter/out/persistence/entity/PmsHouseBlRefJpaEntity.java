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

import java.math.BigDecimal;

/**
 * fms.house_bl 읽기 전용 참조 엔티티.
 * B/L 식별 정보·화물 수치·영업 속성 조회에 사용.
 * 날짜 컬럼(etd/eta)은 VARCHAR(8) yyyyMMdd 저장 컨벤션.
 */
@Entity
@Immutable
@Table(schema = "fms", name = "house_bl")
@Getter
@NoArgsConstructor
public class PmsHouseBlRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_id")
    private Long houseBlId;

    @Column(name = "hbl_no", length = 50)
    private String hblNo;

    @Column(name = "mbl_no", length = 50)
    private String mblNo;

    @Column(name = "job_div", length = 10)
    private String jobDiv;

    @Column(name = "bound", length = 3)
    private String bound;

    @Column(name = "etd", length = 8)
    private String etd;

    @Column(name = "eta", length = 8)
    private String eta;

    @Column(name = "pol_code", length = 10)
    private String polCode;

    @Column(name = "pod_code", length = 10)
    private String podCode;

    @Column(name = "sales_man_code", length = 50)
    private String salesManCode;

    @Column(name = "incoterms", length = 10)
    private String incoterms;

    @Column(name = "sales_class", length = 20)
    private String salesClass;

    @Column(name = "pkg_qty")
    private Integer pkgQty;

    @Column(name = "cbm", precision = 12, scale = 4)
    private BigDecimal cbm;

    @Column(name = "gross_weight_kg", precision = 12, scale = 4)
    private BigDecimal grossWeightKg;

    @Column(name = "actual_customer_code", length = 40)
    private String actualCustomerCode;

    @Column(name = "operator_code", length = 50)
    private String operatorCode;

    @Column(name = "team_code", length = 20)
    private String teamCode;
}
