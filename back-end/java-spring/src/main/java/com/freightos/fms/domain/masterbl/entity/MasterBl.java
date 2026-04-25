package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.FreightTerm;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * E-01 Master B/L 공통 본체.
 * House B/L 계층과 별개 엔티티 계층.
 * PRD §S-04: Party 3슬롯 (Shipper / Consignee / Notify, DOC Partner 없음).
 */
@Entity
@Table(name = "master_bl")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "job_div", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MasterBl extends BaseEntity {

    @Column(name = "mbl_no", length = 50)
    private String mblNo;              // 해상 수출: 미입력 허용 / 기타: 필수

    @Column(name = "master_ref_no", length = 50)
    private String masterRefNo;        // 해상 수출 자동 채번

    @Column(name = "job_div", insertable = false, updatable = false, length = 10)
    private String jobDiv;

    @Column(name = "bound", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Bound bound;

    // ── 당사자 (3슬롯) ──────────────────────────────────────────
    @Column(name = "shipper_code",   length = 20) private String shipperCode;
    @Column(name = "consignee_code", length = 20) private String consigneeCode;
    @Column(name = "notify_code",    length = 20) private String notifyCode;

    // ── 경로 ──────────────────────────────────────────────────────
    @Column(name = "pol_code", length = 10) private String polCode;
    @Column(name = "pod_code", length = 10) private String podCode;

    // ── 일정 ──────────────────────────────────────────────────────
    @Column(name = "etd") private LocalDate etd;
    @Column(name = "eta") private LocalDate eta;

    // ── 운임 ──────────────────────────────────────────────────────
    @Column(name = "freight_term", length = 10)
    @Enumerated(EnumType.STRING)
    private FreightTerm freightTerm;

    // ── 담당 ──────────────────────────────────────────────────────
    @Column(name = "operator_code", length = 20) private String operatorCode;
    @Column(name = "team_code",     length = 20) private String teamCode;

    // ── Cargo 요약 ─────────────────────────────────────────────
    @Column(name = "pkg_qty") private Integer pkgQty;
    @Column(name = "pkg_unit", length = 10) private String pkgUnit;
    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)") private java.math.BigDecimal grossWeightKg;
    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)") private java.math.BigDecimal cbm;

    protected MasterBl(Bound bound) {
        this.bound = bound;
    }
}
