package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — House B/L 화물 표시 및 품목 설명 (E-13).
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(name = "house_bl_desc")
@Getter
@NoArgsConstructor
public class HouseBlDescJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_desc_id", updatable = false, nullable = false)
    private Long houseBlDescId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true)
    private HouseBlJpaEntity houseBl;

    @Column(name = "marks", columnDefinition = "TEXT")
    private String marks;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "desc_clause_1", length = 50)
    private String descClause1;

    @Column(name = "desc_clause_2", length = 50)
    private String descClause2;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setMarks(String v) { this.marks = v; }
    public void setDescription(String v) { this.description = v; }
    public void setDescClause1(String v) { this.descClause1 = v; }
    public void setDescClause2(String v) { this.descClause2 = v; }
    public void setRemark(String v) { this.remark = v; }
}
