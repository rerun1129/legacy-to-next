package com.freightos.fms.adapter.out.persistence.switchbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Switch B/L Description.
 * SwitchBlJpaEntity 와 @OneToOne(FK: switch_bl_id) 관계.
 */
@Entity
@Table(name = "switch_bl_description")
@Getter
@NoArgsConstructor
public class SwitchBlDescriptionJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "switch_bl_description_id", updatable = false, nullable = false)
    private Long switchBlDescriptionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "switch_bl_id", nullable = false, unique = true)
    private SwitchBlJpaEntity switchBl;

    @Column(name = "marks_left", columnDefinition = "TEXT")
    private String marksLeft;

    @Column(name = "marks_right", columnDefinition = "TEXT")
    private String marksRight;

    @Column(name = "nature_quantity_left", columnDefinition = "TEXT")
    private String natureQuantityLeft;

    @Column(name = "nature_quantity_right", columnDefinition = "TEXT")
    private String natureQuantityRight;

    public void setSwitchBl(SwitchBlJpaEntity v)          { this.switchBl           = v; }
    public void setMarksLeft(String v)                    { this.marksLeft           = v; }
    public void setMarksRight(String v)                   { this.marksRight          = v; }
    public void setNatureQuantityLeft(String v)           { this.natureQuantityLeft  = v; }
    public void setNatureQuantityRight(String v)          { this.natureQuantityRight = v; }
}
