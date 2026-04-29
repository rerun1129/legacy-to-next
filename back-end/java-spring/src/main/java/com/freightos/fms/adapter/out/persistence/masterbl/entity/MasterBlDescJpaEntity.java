package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Master B/L 설명 (E-06, Mark & Description).
 * MasterBlJpaEntity 와 @OneToOne(FK: master_bl_id) 관계.
 */
@Entity
@Table(name = "master_bl_desc")
@Getter
@NoArgsConstructor
public class MasterBlDescJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_desc_id", updatable = false, nullable = false)
    private Long masterBlDescId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_bl_id", nullable = false, unique = true)
    private MasterBlJpaEntity masterBl;

    @Column(name = "marks_left", columnDefinition = "TEXT")
    private String marksLeft;

    @Column(name = "marks_right", columnDefinition = "TEXT")
    private String marksRight;

    @Column(name = "desc_left", columnDefinition = "TEXT")
    private String descLeft;

    @Column(name = "desc_right", columnDefinition = "TEXT")
    private String descRight;

    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setMarksLeft(String v) { this.marksLeft = v; }
    public void setMarksRight(String v) { this.marksRight = v; }
    public void setDescLeft(String v) { this.descLeft = v; }
    public void setDescRight(String v) { this.descRight = v; }
}
