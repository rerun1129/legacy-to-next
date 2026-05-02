package com.freightos.fms.domain.switchbl.entity;

import com.freightos.common.entity.BaseEntity;
import com.freightos.fms.domain.common.vo.CustomerCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-21 Switch B/L 본체.
 * House B/L 1건에 1:1로 연결되는 독립 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SwitchBl extends BaseEntity {

    private Long switchBlId;
    private Long houseBlId;
    private String switchBlNo;
    private String blType;
    private String incoterms;
    private CustomerCode shipperCode;
    private CustomerCode consigneeCode;
    private CustomerCode notifyCode;

    private SwitchBlDescription description;

    protected SwitchBl(Long houseBlId, CustomerCode shipperCode) {
        this.houseBlId   = houseBlId;
        this.shipperCode = shipperCode;
    }

    /** 필수값(houseBlId, shipperCode)만으로 생성. 나머지 상세는 updateDetails로 채운다. */
    public static SwitchBl create(Long houseBlId, CustomerCode shipperCode) {
        return new SwitchBl(houseBlId, shipperCode);
    }

    /**
     * 매퍼가 JPA→Domain 변환 시 PK를 주입할 때 사용한다.
     * 어댑터 계층에서만 호출해야 한다.
     */
    public void assignSwitchBlId(Long switchBlId) {
        this.switchBlId = switchBlId;
    }

    public void updateDetails(String switchBlNo, String blType, String incoterms,
                              CustomerCode shipperCode, CustomerCode consigneeCode, CustomerCode notifyCode) {
        this.switchBlNo    = switchBlNo;
        this.blType        = blType;
        this.incoterms     = incoterms;
        this.shipperCode   = shipperCode;
        this.consigneeCode = consigneeCode;
        this.notifyCode    = notifyCode;
    }

    /** 매퍼에서 description 도메인 객체를 연결할 때 사용한다. */
    public void attachDescription(SwitchBlDescription description) {
        this.description = description;
    }
}
