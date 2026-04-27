package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — House B/L Non-B/L 확장.
 */
@Entity
@Table(name = "house_bl_non_bl")
@DiscriminatorValue("NON_BL")
@PrimaryKeyJoinColumn(name = "house_bl_id")
@Getter
@NoArgsConstructor
public class HouseBlNonBlJpaEntity extends HouseBlJpaEntity {

    @Column(name = "work_division", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private HouseBlNonBl.WorkDivision workDivision;

    @Column(name = "settle_partner_code", length = 20)
    private String settlePartnerCode;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "original_bl_ref", length = 50)
    private String originalBlRef;

    public void setWorkDivision(HouseBlNonBl.WorkDivision v) { this.workDivision = v; }
    public void setSettlePartnerCode(String v) { this.settlePartnerCode = v; }
    public void setStatus(String v) { this.status = v; }
    public void setOriginalBlRef(String v) { this.originalBlRef = v; }
}
