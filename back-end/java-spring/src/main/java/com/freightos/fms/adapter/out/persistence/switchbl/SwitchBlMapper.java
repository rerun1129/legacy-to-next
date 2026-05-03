package com.freightos.fms.adapter.out.persistence.switchbl;

import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlDescriptionJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlJpaEntity;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.entity.SwitchBlDescription;
import org.springframework.stereotype.Component;

import static com.freightos.common.util.VoMapper.mapOrNull;

/**
 * JPA ↔ Domain 변환 매퍼 — Switch B/L.
 * toDomain: JPA 엔티티를 도메인 객체로 변환.
 * applyFields: PersistenceAdapter에서 upsert 시 도메인 값을 JPA 엔티티에 적용.
 */
@Component
public class SwitchBlMapper {

    public SwitchBl toDomain(SwitchBlJpaEntity jpa) {
        SwitchBl domain = SwitchBl.create(
                jpa.getHouseBl().getHouseBlId(),
                CustomerCode.of(jpa.getShipperCode(), jpa.getShipperAddress()));
        domain.assignIdentity(jpa.getSwitchBlId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        domain.assignSwitchBlId(jpa.getSwitchBlId());
        domain.updateDetails(jpa.getSwitchBlNo(), jpa.getBlType(), jpa.getIncoterms(),
                CustomerCode.of(jpa.getShipperCode(), jpa.getShipperAddress()),
                CustomerCode.of(jpa.getConsigneeCode(), jpa.getConsigneeAddress()),
                CustomerCode.of(jpa.getNotifyCode(), jpa.getNotifyAddress()));

        SwitchBlDescriptionJpaEntity descJpa = jpa.getDescription();
        if (descJpa != null) {
            domain.attachDescription(toDescriptionDomain(descJpa));
        }
        return domain;
    }

    private SwitchBlDescription toDescriptionDomain(SwitchBlDescriptionJpaEntity jpa) {
        SwitchBlDescription desc = SwitchBlDescription.create(jpa.getSwitchBl().getSwitchBlId());
        desc.assignIdentity(jpa.getSwitchBlDescriptionId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                jpa.getCreatedBy(), jpa.getUpdatedBy());
        desc.assignSwitchBlDescriptionId(jpa.getSwitchBlDescriptionId());
        desc.updateContent(jpa.getMarks(), jpa.getNatureQuantity());
        return desc;
    }

    public void applyFields(SwitchBl domain, SwitchBlJpaEntity jpa) {
        jpa.setSwitchBlNo(domain.getSwitchBlNo());
        jpa.setBlType(domain.getBlType());
        jpa.setIncoterms(domain.getIncoterms());
        jpa.setShipperCode(mapOrNull(domain.getShipperCode(), CustomerCode::value));
        jpa.setShipperAddress(mapOrNull(domain.getShipperCode(), CustomerCode::address));
        jpa.setConsigneeCode(mapOrNull(domain.getConsigneeCode(), CustomerCode::value));
        jpa.setConsigneeAddress(mapOrNull(domain.getConsigneeCode(), CustomerCode::address));
        jpa.setNotifyCode(mapOrNull(domain.getNotifyCode(), CustomerCode::value));
        jpa.setNotifyAddress(mapOrNull(domain.getNotifyCode(), CustomerCode::address));
    }

    public SwitchBlDescriptionJpaEntity toDescriptionJpa(SwitchBlDescription desc, SwitchBlJpaEntity switchBlJpa) {
        SwitchBlDescriptionJpaEntity jpa = new SwitchBlDescriptionJpaEntity();
        jpa.setSwitchBl(switchBlJpa);
        jpa.setMarks(desc.getMarks());
        jpa.setNatureQuantity(desc.getNatureQuantity());
        return jpa;
    }
}
