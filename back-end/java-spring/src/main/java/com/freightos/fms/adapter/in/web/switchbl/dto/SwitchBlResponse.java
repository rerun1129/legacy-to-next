package com.freightos.fms.adapter.in.web.switchbl.dto;

import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.entity.SwitchBlDescription;

import static com.freightos.common.util.VoMapper.mapOrNull;

/** Switch B/L 응답 DTO. */
public record SwitchBlResponse(
        Long id,
        Long switchBlId,
        Long houseBlId,
        String switchBlNo,
        String shipperCode,
        String shipperAddress,
        String consigneeCode,
        String consigneeAddress,
        String notifyCode,
        String notifyAddress,
        String marks,
        String natureQuantity
) {
    public static SwitchBlResponse from(SwitchBl domain) {
        SwitchBlDescription desc = domain.getDescription();
        return new SwitchBlResponse(
                domain.getId(),
                domain.getSwitchBlId(),
                domain.getHouseBlId(),
                domain.getSwitchBlNo(),
                mapOrNull(domain.getShipperCode(), CustomerCode::value),
                mapOrNull(domain.getShipperCode(), CustomerCode::address),
                mapOrNull(domain.getConsigneeCode(), CustomerCode::value),
                mapOrNull(domain.getConsigneeCode(), CustomerCode::address),
                mapOrNull(domain.getNotifyCode(), CustomerCode::value),
                mapOrNull(domain.getNotifyCode(), CustomerCode::address),
                desc != null ? desc.getMarks() : null,
                desc != null ? desc.getNatureQuantity() : null
        );
    }
}
