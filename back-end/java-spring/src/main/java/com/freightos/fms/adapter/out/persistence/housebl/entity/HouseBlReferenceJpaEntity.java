package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM ?뷀떚????House B/L 李몄“ 踰덊샇 (E-18).
 */
@Entity
@Table(name = "house_bl_reference")
@Getter
@NoArgsConstructor
public class HouseBlReferenceJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_reference_id", updatable = false, nullable = false)
    private Long houseBlReferenceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false)
    private HouseBlJpaEntity houseBl;

    @Column(name = "reference_type", nullable = false, length = 20)
    private String referenceType;

    @Column(name = "reference_no", nullable = false, length = 100)
    private String referenceNo;

    @Column(name = "seq", nullable = false)
    private int seq;

    public void setHouseBlReferenceId(Long v) { this.houseBlReferenceId = v; }
    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setReferenceType(String v) { this.referenceType = v; }
    public void setReferenceNo(String v) { this.referenceNo = v; }
    public void setSeq(int v) { this.seq = v; }
}

