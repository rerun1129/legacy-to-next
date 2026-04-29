package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — E-13 House B/L 설명 (Marks / Description / Clause / Remark).
 */
@Entity
@Table(name = "house_bl_desc")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlDescJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_desc_id", updatable = false, nullable = false)
    private Long houseBlDescId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false)
    private HouseBlJpaEntity houseBl;

    @Column(name = "marks", columnDefinition = "TEXT")
    private String marks;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "desc_clause", columnDefinition = "TEXT")
    private String descClause;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    public void setHouseBlDescId(Long v) { this.houseBlDescId = v; }
    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setMarks(String v) { this.marks = v; }
    public void setDescription(String v) { this.description = v; }
    public void setDescClause(String v) { this.descClause = v; }
    public void setRemark(String v) { this.remark = v; }
}
