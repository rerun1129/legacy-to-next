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
 * fms.master_bl 읽기 전용 참조 엔티티.
 * BMS 조회 시 bl_id(MASTER) → jobDiv·bound·mblNo·etd·eta 파생 정보 추출에만 사용.
 * cross-schema 접근은 codename 패키지에만 격리.
 * fms.master_bl에 deleted_at 없음 — 활성 필터 미적용.
 */
@Entity
@Immutable
@Table(schema = "fms", name = "master_bl")
@Getter
@NoArgsConstructor
public class MasterBlRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_id")
    private Long masterBlId;

    @Column(name = "job_div", nullable = false, length = 10)
    private String jobDiv;

    @Column(name = "bound", nullable = false, length = 3)
    private String bound;

    @Column(name = "mbl_no", length = 50)
    private String mblNo;

    @Column(name = "etd", length = 8)
    private String etd;

    @Column(name = "eta", length = 8)
    private String eta;
}
