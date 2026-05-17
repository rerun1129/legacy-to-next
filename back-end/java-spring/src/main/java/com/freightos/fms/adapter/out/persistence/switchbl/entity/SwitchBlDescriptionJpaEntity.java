package com.freightos.fms.adapter.out.persistence.switchbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Switch B/L Description.
 * SwitchBlJpaEntity 와 @OneToOne(FK: switch_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "switch_bl_description")
@Getter
@NoArgsConstructor
public class SwitchBlDescriptionJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "switch_bl_description_id", updatable = false, nullable = false)
    private Long switchBlDescriptionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "switch_bl_id", nullable = false, unique = true, updatable = false)
    private SwitchBlJpaEntity switchBl;

    @Column(name = "marks", columnDefinition = "TEXT")
    private String marks;

    @Column(name = "nature_quantity", columnDefinition = "TEXT")
    private String natureQuantity;

    public void setSwitchBl(SwitchBlJpaEntity v)    { this.switchBl      = v; }
    public void setMarks(String v)                  { this.marks         = v; }
    public void setNatureQuantity(String v)         { this.natureQuantity = v; }
}
