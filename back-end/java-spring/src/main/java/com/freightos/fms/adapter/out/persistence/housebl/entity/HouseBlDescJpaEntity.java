package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM ?뷀떚????House B/L ?붾Ъ ?쒖떆 諛??덈ぉ ?ㅻ챸 (E-13).
 * HouseBlJpaEntity ? @OneToOne(FK: house_bl_id) 愿怨?
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

    @Column(name = "marks_left", columnDefinition = "TEXT")
    private String marksLeft;

    @Column(name = "marks_right", columnDefinition = "TEXT")
    private String marksRight;

    @Column(name = "description_left", columnDefinition = "TEXT")
    private String descriptionLeft;

    @Column(name = "description_right", columnDefinition = "TEXT")
    private String descriptionRight;

    @Column(name = "desc_clause_1", length = 50)
    private String descClause1;

    @Column(name = "desc_clause_2", length = 50)
    private String descClause2;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    public void setHouseBlDescId(Long v) { this.houseBlDescId = v; }
    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setMarksLeft(String v) { this.marksLeft = v; }
    public void setMarksRight(String v) { this.marksRight = v; }
    public void setDescriptionLeft(String v) { this.descriptionLeft = v; }
    public void setDescriptionRight(String v) { this.descriptionRight = v; }
    public void setDescClause1(String v) { this.descClause1 = v; }
    public void setDescClause2(String v) { this.descClause2 = v; }
    public void setRemark(String v) { this.remark = v; }
}

