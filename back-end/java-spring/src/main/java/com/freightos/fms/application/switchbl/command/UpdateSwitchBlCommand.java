package com.freightos.fms.application.switchbl.command;

/**
 * Switch B/L 수정 커맨드. null 필드는 기존 값 유지(PATCH 의미론).
 */
public record UpdateSwitchBlCommand(
        String switchBlNo,
        String shipperCode,
        String shipperAddress,
        String consigneeCode,
        String consigneeAddress,
        String notifyCode,
        String notifyAddress,
        DescriptionCommand description
) {
    public record DescriptionCommand(
            String marks,
            String natureQuantity
    ) {}
}
