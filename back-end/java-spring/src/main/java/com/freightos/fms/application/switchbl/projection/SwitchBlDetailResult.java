package com.freightos.fms.application.switchbl.projection;

/**
 * Switch B/L 단건 조회·수정 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * 모든 필드는 primitive — domain VO 미포함.
 */
public record SwitchBlDetailResult(
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
        String marks,
        String natureQuantity
) {}
