package com.freightos.fms.adapter.in.web.switchbl.dto;

import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;

/** Switch B/L 응답 DTO. */
public record SwitchBlResponse(
        Long id,
        Long switchBlId,
        Long houseBlId,
        String switchBlNo,
        String blType,
        String incoterms,
        String shipperCode,
        String shipperAddress,
        String consigneeCode,
        String consigneeAddress,
        String notifyCode,
        String notifyAddress,
        SwitchBlDescriptionDto description
) {
    public static SwitchBlResponse from(SwitchBlDetailResult result) {
        SwitchBlDescriptionDto description = (result.marks() != null || result.natureQuantity() != null)
                ? new SwitchBlDescriptionDto(result.marks(), result.natureQuantity())
                : null;
        return new SwitchBlResponse(
                result.id(),
                result.switchBlId(),
                result.houseBlId(),
                result.switchBlNo(),
                result.blType(),
                result.incoterms(),
                result.shipperCode(),
                result.shipperAddress(),
                result.consigneeCode(),
                result.consigneeAddress(),
                result.notifyCode(),
                result.notifyAddress(),
                description
        );
    }
}
