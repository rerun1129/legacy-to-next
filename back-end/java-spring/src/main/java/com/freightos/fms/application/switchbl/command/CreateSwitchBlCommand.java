package com.freightos.fms.application.switchbl.command;

/**
 * Switch B/L 생성 커맨드. DTO→도메인 변환 책임을 Application 계층(Factory)으로 격리하기 위한 중간 표현.
 * validation 어노테이션 없음 — 검증은 Request DTO에서 수행 완료.
 */
public record CreateSwitchBlCommand(
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
        DescriptionCommand description
) {
    public record DescriptionCommand(
            String marks,
            String natureQuantity
    ) {}
}
