package com.freightos.fms.adapter.in.web.switchbl;

import com.freightos.fms.adapter.in.web.switchbl.dto.CreateSwitchBlRequest;
import com.freightos.fms.adapter.in.web.switchbl.dto.SwitchBlDescriptionDto;
import com.freightos.fms.adapter.in.web.switchbl.dto.SwitchBlResponse;
import com.freightos.fms.adapter.in.web.switchbl.dto.UpdateSwitchBlRequest;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.entity.SwitchBlDescription;
import org.springframework.stereotype.Component;

/**
 * 도메인 엔티티를 Switch B/L 응답 DTO로 변환하고, 요청 DTO를 도메인 엔티티로 변환한다.
 * 컨트롤러는 매핑을 직접 호출하지 않고 본 어셈블러에 위임한다.
 */
@Component
public class SwitchBlAssembler {

    /** CREATE 요청을 도메인 엔티티로 변환한다. */
    public SwitchBl toEntity(CreateSwitchBlRequest req) {
        SwitchBl entity = SwitchBl.create(
                req.houseBlId(),
                CustomerCode.of(req.shipperCode(), req.shipperAddress())
        );
        entity.updateDetails(
                req.switchBlNo(),
                req.blType(),
                req.incoterms(),
                CustomerCode.of(req.shipperCode(), req.shipperAddress()),
                CustomerCode.of(req.consigneeCode(), req.consigneeAddress()),
                CustomerCode.of(req.notifyCode(), req.notifyAddress())
        );
        if (req.description() != null) {
            entity.attachDescription(toDescriptionEntity(req.description()));
        }
        return entity;
    }

    /**
     * UPDATE 요청을 기존 엔티티에 반영한다.
     * null 필드는 기존 값을 유지한다 (PATCH 의미론).
     */
    public void applyToEntity(UpdateSwitchBlRequest req, SwitchBl entity) {
        entity.updateDetails(
                req.switchBlNo()   != null ? req.switchBlNo()   : entity.getSwitchBlNo(),
                req.blType()       != null ? req.blType()       : entity.getBlType(),
                req.incoterms()    != null ? req.incoterms()    : entity.getIncoterms(),
                req.shipperCode()  != null
                        ? CustomerCode.of(req.shipperCode(), req.shipperAddress())
                        : entity.getShipperCode(),
                req.consigneeCode() != null
                        ? CustomerCode.of(req.consigneeCode(), req.consigneeAddress())
                        : entity.getConsigneeCode(),
                req.notifyCode()   != null
                        ? CustomerCode.of(req.notifyCode(), req.notifyAddress())
                        : entity.getNotifyCode()
        );
        if (req.description() != null) {
            entity.attachDescription(toDescriptionEntity(req.description()));
        }
    }

    public SwitchBlResponse toResponse(SwitchBl domain) {
        return SwitchBlResponse.from(domain);
    }

    private SwitchBlDescription toDescriptionEntity(SwitchBlDescriptionDto dto) {
        // switchBlId는 저장 이후 PersistenceAdapter가 연결하므로 여기서는 null 허용
        SwitchBlDescription desc = SwitchBlDescription.create(null);
        desc.updateContent(dto.marks(), dto.natureQuantity());
        return desc;
    }
}
