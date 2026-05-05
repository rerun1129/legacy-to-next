package com.freightos.fms.adapter.in.web.switchbl.dto;

import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;

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
    public static SwitchBlResponse from(SwitchBlDetailResult result) {
        return new SwitchBlResponse(
                result.id(),
                result.switchBlId(),
                result.houseBlId(),
                result.switchBlNo(),
                result.shipperCode(),
                result.shipperAddress(),
                result.consigneeCode(),
                result.consigneeAddress(),
                result.notifyCode(),
                result.notifyAddress(),
                result.marks(),
                result.natureQuantity()
        );
    }
}
