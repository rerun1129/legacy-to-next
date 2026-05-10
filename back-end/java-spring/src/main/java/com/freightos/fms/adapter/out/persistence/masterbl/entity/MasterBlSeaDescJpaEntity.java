package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Master B/L 해상(SEA) 화물 표시 및 품목 설명.
 * MasterBlSeaJpaEntity 와 @OneToOne(FK: master_bl_sea_id) 관계.
 * ON DELETE CASCADE — seaExt 삭제 시 DB가 자동 정리.
 */
@Entity
@Table(schema = "fms", name = "master_bl_sea_desc")
@Getter
@NoArgsConstructor
public class MasterBlSeaDescJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_sea_desc_id", updatable = false, nullable = false)
    private Long masterBlSeaDescId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_bl_sea_id", nullable = false, unique = true)
    private MasterBlSeaJpaEntity sea;

    @Column(name = "marks", columnDefinition = "TEXT")
    private String marks;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "desc_clause_1")
    private DescClause1 descClause1;

    @Enumerated(EnumType.STRING)
    @Column(name = "desc_clause_2")
    private DescClause2 descClause2;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    public void setMasterBlSeaDescId(Long v) { this.masterBlSeaDescId = v; }
    public void setSea(MasterBlSeaJpaEntity v) { this.sea = v; }
    public void setMarks(String v) { this.marks = v; }
    public void setDescription(String v) { this.description = v; }
    public void setDescClause1(DescClause1 v) { this.descClause1 = v; }
    public void setDescClause2(DescClause2 v) { this.descClause2 = v; }
    public void setRemark(String v) { this.remark = v; }
}
