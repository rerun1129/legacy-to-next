package com.freightos.fms.adapter.in.web.switchbl.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Switch B/L 수정 요청 DTO.
 * houseBlId는 변경 불가 — path variable의 id로 기존 엔티티를 조회한 후 필드만 덮어씀.
 */
public record UpdateSwitchBlRequest(
        @NotBlank @Size(max = 50) String switchBlNo,
        @Size(max = 20) String shipperCode,
        @Size(max = 500) String shipperAddress,
        @Size(max = 20) String consigneeCode,
        @Size(max = 500) String consigneeAddress,
        @Size(max = 20) String notifyCode,
        @Size(max = 500) String notifyAddress,
        @Valid SwitchBlDescriptionDto description
) {}
